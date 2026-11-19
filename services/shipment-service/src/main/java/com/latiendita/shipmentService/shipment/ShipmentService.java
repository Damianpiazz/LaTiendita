package com.latiendita.shipmentService.shipment;

import com.latiendita.shipmentService.CurrentUser;
import com.latiendita.shipmentService.address.DeliveryAddressEntity;
import com.latiendita.shipmentService.address.DeliveryAddressService;
import com.latiendita.shipmentService.consumer.event.OrderCancelledEvent;
import com.latiendita.shipmentService.consumer.event.PaymentApprovedEvent;
import com.latiendita.shipmentService.event.ShipmentCancelledEvent;
import com.latiendita.shipmentService.event.ShipmentDeliveredEvent;
import com.latiendita.shipmentService.event.ShipmentDispatchedEvent;
import com.latiendita.shipmentService.exception.DuplicateShipmentException;
import com.latiendita.shipmentService.exception.InvalidShipmentRequestException;
import com.latiendita.shipmentService.exception.InvalidShipmentStateException;
import com.latiendita.shipmentService.exception.ShipmentAccessDeniedException;
import com.latiendita.shipmentService.exception.ShipmentNotFoundException;
import com.latiendita.shipmentService.order.OrderClient;
import com.latiendita.shipmentService.outbox.OutboxWriter;
import com.latiendita.shipmentService.shipment.dto.CreateShipmentRequest;
import com.latiendita.shipmentService.shipment.dto.QuoteRequest;
import com.latiendita.shipmentService.shipment.dto.QuoteResponse;
import com.latiendita.shipmentService.shipment.dto.ShipmentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    public static final String SHIPMENT_EVENTS_TOPIC = "shipment-events";
    public static final String SHIPMENT_DISPATCHED = "SHIPMENT_DISPATCHED";
    public static final String SHIPMENT_DELIVERED = "SHIPMENT_DELIVERED";
    public static final String SHIPMENT_CANCELLED = "SHIPMENT_CANCELLED";

    private final ShipmentRepository shipmentRepository;
    private final DeliveryAddressService addressService;
    private final OrderClient orderClient;
    private final OutboxWriter outboxWriter;
    private final QuoteService quoteService;

    @Transactional
    public ShipmentResponse create(CreateShipmentRequest request) {
        Long orderId = request.orderId();
        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new DuplicateShipmentException(orderId);
        }

        OrderClient.OrderDto order = orderClient.findById(orderId);
        if (!"PENDING".equals(order.status())) {
            throw new InvalidShipmentStateException(
                    "El pedido " + orderId + " debe estar PENDING para registrar el envío");
        }

        DeliveryAddressEntity source = resolveAddress(request);
        Instant now = Instant.now();
        ShipmentEntity shipment = ShipmentEntity.builder()
                .orderId(orderId)
                .customerId(CurrentUser.id())
                .status(ShipmentStatus.AWAITING_PAYMENT)
                .shippingCost(request.shippingCost())
                .currency(request.currency())
                .recipientName(source.getRecipientName())
                .phone(source.getPhone())
                .addressLine(source.getAddressLine())
                .city(source.getCity())
                .province(source.getProvince())
                .postalCode(source.getPostalCode())
                .references(source.getReferences())
                .estimatedFrom(request.estimatedFrom())
                .estimatedTo(request.estimatedTo())
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findById(Long id) {
        ShipmentEntity shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
        requireOwner(shipment);
        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findByOrderId(Long orderId) {
        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException(orderId, true));
        requireOwner(shipment);
        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> list(Pageable pageable) {
        return shipmentRepository.findAll(pageable).map(ShipmentService::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> listByStatus(ShipmentStatus status, Pageable pageable) {
        return shipmentRepository.findByStatus(status, pageable).map(ShipmentService::toResponse);
    }

    @Transactional(readOnly = true)
    public long countPendingDispatch() {
        return shipmentRepository.countByStatus(ShipmentStatus.PENDING_DISPATCH);
    }

    @Transactional
    public ShipmentResponse dispatch(Long id) {
        ShipmentEntity shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));

        if (shipment.getStatus() == ShipmentStatus.DISPATCHED) {
            return toResponse(shipment);
        }
        if (shipment.getStatus() != ShipmentStatus.PENDING_DISPATCH) {
            throw new InvalidShipmentStateException(
                    "No se puede despachar un envío en estado " + shipment.getStatus());
        }

        Instant now = Instant.now();
        shipment.setStatus(ShipmentStatus.DISPATCHED);
        shipment.setDispatchedAt(now);
        shipment.setUpdatedAt(now);
        ShipmentResponse response = toResponse(shipmentRepository.save(shipment));

        ShipmentDispatchedEvent event = new ShipmentDispatchedEvent(
                SHIPMENT_DISPATCHED, shipment.getId(), shipment.getOrderId(),
                shipment.getCustomerId(), shipment.getDispatchedAt(),
                shipment.getEstimatedTo(), Instant.now());
        outboxWriter.append(SHIPMENT_EVENTS_TOPIC, shipment.getOrderId(),
                SHIPMENT_DISPATCHED, event);
        return response;
    }

    @Transactional
    public ShipmentResponse markDelivered(Long id) {
        ShipmentEntity shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            return toResponse(shipment);
        }
        if (shipment.getStatus() != ShipmentStatus.DISPATCHED) {
            throw new InvalidShipmentStateException(
                    "No se puede entregar un envío en estado " + shipment.getStatus());
        }

        Instant now = Instant.now();
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(now);
        shipment.setUpdatedAt(now);
        ShipmentResponse response = toResponse(shipmentRepository.save(shipment));

        ShipmentDeliveredEvent event = new ShipmentDeliveredEvent(
                SHIPMENT_DELIVERED, shipment.getId(), shipment.getOrderId(),
                shipment.getCustomerId(), shipment.getDeliveredAt(), Instant.now());
        outboxWriter.append(SHIPMENT_EVENTS_TOPIC, shipment.getOrderId(),
                SHIPMENT_DELIVERED, event);
        return response;
    }

    @Transactional
    public void approvePayment(PaymentApprovedEvent event) {
        ShipmentEntity shipment = shipmentRepository.findByOrderId(event.orderId()).orElse(null);
        if (shipment != null) {
            if (shipment.getStatus() == ShipmentStatus.AWAITING_PAYMENT) {
                shipment.setStatus(ShipmentStatus.PENDING_DISPATCH);
                shipment.setUpdatedAt(Instant.now());
                shipmentRepository.save(shipment);
            }
            return;
        }

        DeliveryAddressEntity address = addressService
                .findDefaultForCustomer(event.customerId()).orElse(null);
        QuoteResponse quote = quoteService.quote(new QuoteRequest(BigDecimal.ZERO));
        Instant now = Instant.now();
        ShipmentEntity created = ShipmentEntity.builder()
                .orderId(event.orderId())
                .customerId(event.customerId())
                .status(ShipmentStatus.PENDING_DISPATCH)
                .shippingCost(event.amount() == null ? quote.shippingCost() : event.amount())
                .currency(event.currency() == null ? quote.currency() : event.currency())
                .recipientName(valueOrEmpty(address == null ? null : address.getRecipientName()))
                .phone(valueOrEmpty(address == null ? null : address.getPhone()))
                .addressLine(valueOrEmpty(address == null ? null : address.getAddressLine()))
                .city(valueOrEmpty(address == null ? null : address.getCity()))
                .province(valueOrEmpty(address == null ? null : address.getProvince()))
                .postalCode(valueOrEmpty(address == null ? null : address.getPostalCode()))
                .references(address == null ? null : address.getReferences())
                .estimatedFrom(quote.estimatedFrom())
                .estimatedTo(quote.estimatedTo())
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
        shipmentRepository.save(created);
    }

    @Transactional
    public void cancelForOrder(OrderCancelledEvent event) {
        ShipmentEntity shipment = shipmentRepository.findByOrderId(event.orderId()).orElse(null);
        if (shipment == null || !shipment.getStatus().canBeCancelled()) {
            return;
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setUpdatedAt(Instant.now());
        shipmentRepository.save(shipment);

        ShipmentCancelledEvent published = new ShipmentCancelledEvent(
                SHIPMENT_CANCELLED, shipment.getId(), shipment.getOrderId(),
                shipment.getCustomerId(), Instant.now());
        outboxWriter.append(SHIPMENT_EVENTS_TOPIC, shipment.getOrderId(),
                SHIPMENT_CANCELLED, published);
    }

    private DeliveryAddressEntity resolveAddress(CreateShipmentRequest request) {
        if (request.addressId() != null) {
            return addressService.findOwned(request.addressId());
        }
        CreateShipmentRequest.AddressInput input = request.address();
        if (input == null) {
            throw new InvalidShipmentRequestException("Debe indicar addressId o address");
        }
        return DeliveryAddressEntity.builder()
                .recipientName(input.recipientName())
                .phone(input.phone())
                .addressLine(input.addressLine())
                .city(input.city())
                .province(input.province())
                .postalCode(input.postalCode())
                .references(input.references())
                .build();
    }

    private void requireOwner(ShipmentEntity shipment) {
        if (!shipment.getCustomerId().equals(CurrentUser.id())) {
            throw new ShipmentAccessDeniedException(shipment.getId());
        }
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static ShipmentResponse toResponse(ShipmentEntity shipment) {
        return new ShipmentResponse(shipment.getId(), shipment.getOrderId(), shipment.getStatus(),
                shipment.getShippingCost(), shipment.getCurrency(), shipment.getRecipientName(),
                shipment.getPhone(), shipment.getAddressLine(), shipment.getCity(),
                shipment.getProvince(), shipment.getPostalCode(), shipment.getReferences(),
                shipment.getEstimatedFrom(), shipment.getEstimatedTo(), shipment.getDispatchedAt(),
                shipment.getDeliveredAt());
    }
}
