package com.latiendita.shipmentService.shipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.shipmentService.CurrentUser;
import com.latiendita.shipmentService.address.DeliveryAddressEntity;
import com.latiendita.shipmentService.address.DeliveryAddressService;
import com.latiendita.shipmentService.consumer.event.OrderCancelledEvent;
import com.latiendita.shipmentService.consumer.event.PaymentApprovedEvent;
import com.latiendita.shipmentService.event.ShipmentCancelledEvent;
import com.latiendita.shipmentService.event.ShipmentDeliveredEvent;
import com.latiendita.shipmentService.event.ShipmentDispatchedEvent;
import com.latiendita.shipmentService.exception.DuplicateShipmentException;
import com.latiendita.shipmentService.exception.InvalidShipmentStateException;
import com.latiendita.shipmentService.exception.ShipmentAccessDeniedException;
import com.latiendita.shipmentService.order.OrderClient;
import com.latiendita.shipmentService.outbox.OutboxWriter;
import com.latiendita.shipmentService.shipment.dto.CreateShipmentRequest;
import com.latiendita.shipmentService.shipment.dto.ShipmentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    private static final String USER = "user-1";

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private DeliveryAddressService addressService;

    @Mock
    private OrderClient orderClient;

    @Mock
    private OutboxWriter outboxWriter;

    private ShipmentService service;

    @BeforeEach
    void setUp() {
        QuoteService quoteService = new QuoteService(new BigDecimal("2499"), 3, 5, "ARS");
        service = new ShipmentService(shipmentRepository, addressService, orderClient,
                outboxWriter, quoteService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CurrentUser.HEADER, USER);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldCopySavedAddressIntoShipmentSnapshot() {
        when(orderClient.findById(7L)).thenReturn(new OrderClient.OrderDto(7L, "PENDING"));
        when(addressService.findOwned(5L)).thenReturn(savedAddress());
        when(shipmentRepository.existsByOrderId(7L)).thenReturn(false);
        when(shipmentRepository.save(any(ShipmentEntity.class))).thenAnswer(invocation -> {
            ShipmentEntity entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        ShipmentResponse response = service.create(createRequest(7L, 5L, null));

        ArgumentCaptor<ShipmentEntity> captor = ArgumentCaptor.forClass(ShipmentEntity.class);
        verify(shipmentRepository).save(captor.capture());
        ShipmentEntity saved = captor.getValue();
        assertThat(saved.getCustomerId()).isEqualTo(USER);
        assertThat(saved.getRecipientName()).isEqualTo("Ana Diaz");
        assertThat(saved.getAddressLine()).isEqualTo("Av. Principal 123");
        assertThat(saved.getPostalCode()).isEqualTo("C1000");
        assertThat(saved.getStatus()).isEqualTo(ShipmentStatus.AWAITING_PAYMENT);
        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.addressLine()).isEqualTo("Av. Principal 123");
        verify(outboxWriter, never()).append(anyString(), any(), anyString(), any());
    }

    @Test
    void shouldAcceptInlineAddressWhenNoSavedAddressGiven() {
        when(orderClient.findById(7L)).thenReturn(new OrderClient.OrderDto(7L, "PENDING"));
        when(shipmentRepository.existsByOrderId(7L)).thenReturn(false);
        when(shipmentRepository.save(any(ShipmentEntity.class))).thenAnswer(invocation -> {
            ShipmentEntity entity = invocation.getArgument(0);
            entity.setId(12L);
            return entity;
        });

        service.create(createRequest(7L, null,
                new CreateShipmentRequest.AddressInput("Inline Owner", "555-0000",
                        "Calle 9", "Cordoba", "Cordoba", "X5000", "Depto 3")));

        ArgumentCaptor<ShipmentEntity> captor = ArgumentCaptor.forClass(ShipmentEntity.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipientName()).isEqualTo("Inline Owner");
        verify(addressService, never()).findOwned(any());
    }

    @Test
    void shouldRejectSecondShipmentForSameOrder() {
        when(shipmentRepository.existsByOrderId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest(7L, 5L, null)))
                .isInstanceOf(DuplicateShipmentException.class)
                .hasMessageContaining("7");

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectShipmentWhenOrderIsNotPending() {
        when(orderClient.findById(7L)).thenReturn(new OrderClient.OrderDto(7L, "PAID"));
        when(shipmentRepository.existsByOrderId(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(createRequest(7L, 5L, null)))
                .isInstanceOf(InvalidShipmentStateException.class)
                .hasMessageContaining("PENDING");

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void shouldDispatchFromPendingDispatchAndPublishEventKeyedByOrderId() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.PENDING_DISPATCH);
        when(shipmentRepository.findById(11L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponse response = service.dispatch(11L);

        assertThat(response.status()).isEqualTo(ShipmentStatus.DISPATCHED);
        assertThat(shipment.getDispatchedAt()).isNotNull();
        ArgumentCaptor<ShipmentDispatchedEvent> captor = ArgumentCaptor.forClass(ShipmentDispatchedEvent.class);
        verify(outboxWriter).append(eq("shipment-events"), eq(7L),
                eq("SHIPMENT_DISPATCHED"), captor.capture());
        ShipmentDispatchedEvent event = captor.getValue();
        assertThat(event.shipmentId()).isEqualTo(11L);
        assertThat(event.orderId()).isEqualTo(7L);
        assertThat(event.customerId()).isEqualTo(USER);
        assertThat(event.dispatchedAt()).isEqualTo(shipment.getDispatchedAt());
        assertThat(event.estimatedTo()).isEqualTo(shipment.getEstimatedTo());
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void shouldRepeatDispatchWithoutRepublishing() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.DISPATCHED);
        when(shipmentRepository.findById(11L)).thenReturn(Optional.of(shipment));

        ShipmentResponse response = service.dispatch(11L);

        assertThat(response.status()).isEqualTo(ShipmentStatus.DISPATCHED);
        verify(outboxWriter, never()).append(anyString(), any(), anyString(), any());
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void shouldPublishDeliveredEventWhenMarkingDelivered() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.DISPATCHED);
        when(shipmentRepository.findById(11L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponse response = service.markDelivered(11L);

        assertThat(response.status()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(shipment.getDeliveredAt()).isNotNull();
        ArgumentCaptor<ShipmentDeliveredEvent> captor =
                ArgumentCaptor.forClass(ShipmentDeliveredEvent.class);
        verify(outboxWriter).append(eq("shipment-events"), eq(7L),
                eq("SHIPMENT_DELIVERED"), captor.capture());
        assertThat(captor.getValue().shipmentId()).isEqualTo(11L);
        assertThat(captor.getValue().deliveredAt()).isEqualTo(shipment.getDeliveredAt());
    }

    @Test
    void shouldRejectDispatchFromAwaitingPayment() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.AWAITING_PAYMENT);
        when(shipmentRepository.findById(11L)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.dispatch(11L))
                .isInstanceOf(InvalidShipmentStateException.class)
                .hasMessageContaining("AWAITING_PAYMENT");

        verify(outboxWriter, never()).append(anyString(), any(), anyString(), any());
    }

    @Test
    void shouldDenyReadingAnotherCustomersShipment() {
        when(shipmentRepository.findById(12L))
                .thenReturn(Optional.of(shipment(12L, "user-2", ShipmentStatus.PENDING_DISPATCH)));

        assertThatThrownBy(() -> service.findById(12L))
                .isInstanceOf(ShipmentAccessDeniedException.class)
                .hasMessageContaining("12");
    }

    @Test
    void shouldMoveShipmentToPendingDispatchOnPaymentApproved() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.AWAITING_PAYMENT);
        when(shipmentRepository.findByOrderId(7L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        service.approvePayment(paymentApproved(7L, USER));

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING_DISPATCH);
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void shouldBeIdempotentOnRepeatedPaymentApproved() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.PENDING_DISPATCH);
        when(shipmentRepository.findByOrderId(7L)).thenReturn(Optional.of(shipment));

        service.approvePayment(paymentApproved(7L, USER));

        verify(shipmentRepository, never()).save(any());
        verify(outboxWriter, never()).append(anyString(), any(), anyString(), any());
    }

    @Test
    void shouldCreateShipmentFromDefaultAddressWhenPaymentApprovedHasNoShipment() {
        when(shipmentRepository.findByOrderId(7L)).thenReturn(Optional.empty());
        when(addressService.findDefaultForCustomer(USER)).thenReturn(Optional.of(savedAddress()));
        when(shipmentRepository.save(any(ShipmentEntity.class))).thenAnswer(i -> i.getArgument(0));

        service.approvePayment(paymentApproved(7L, USER));

        ArgumentCaptor<ShipmentEntity> captor = ArgumentCaptor.forClass(ShipmentEntity.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ShipmentStatus.PENDING_DISPATCH);
        assertThat(captor.getValue().getRecipientName()).isEqualTo("Ana Diaz");
    }

    @Test
    void shouldCancelCancellableShipmentAndPublishEventOnlyOnce() {
        ShipmentEntity shipment = shipment(11L, USER, ShipmentStatus.PENDING_DISPATCH);
        when(shipmentRepository.findByOrderId(7L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        service.cancelForOrder(orderCancelled(7L));
        service.cancelForOrder(orderCancelled(7L));

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        ArgumentCaptor<ShipmentCancelledEvent> captor = ArgumentCaptor.forClass(ShipmentCancelledEvent.class);
        verify(outboxWriter, times(1)).append(eq("shipment-events"), eq(7L),
                eq("SHIPMENT_CANCELLED"), captor.capture());
        assertThat(captor.getValue().shipmentId()).isEqualTo(11L);
        verify(shipmentRepository, times(1)).save(shipment);
    }

    @Test
    void shouldCountPendingDispatchShipments() {
        when(shipmentRepository.countByStatus(ShipmentStatus.PENDING_DISPATCH)).thenReturn(3L);

        assertThat(service.countPendingDispatch()).isEqualTo(3L);
    }

    private CreateShipmentRequest createRequest(Long orderId, Long addressId,
                                                CreateShipmentRequest.AddressInput address) {
        return new CreateShipmentRequest(orderId, addressId, address,
                new BigDecimal("2499"), "ARS",
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 19));
    }

    static DeliveryAddressEntity savedAddress() {
        return DeliveryAddressEntity.builder()
                .id(5L)
                .customerId(USER)
                .recipientName("Ana Diaz")
                .phone("555-1234")
                .addressLine("Av. Principal 123")
                .city("Buenos Aires")
                .province("CABA")
                .postalCode("C1000")
                .isDefault(true)
                .build();
    }

    static ShipmentEntity shipment(Long id, String customerId, ShipmentStatus status) {
        ShipmentEntity entity = ShipmentEntity.builder()
                .id(id)
                .orderId(7L)
                .customerId(customerId)
                .status(status)
                .shippingCost(new BigDecimal("2499"))
                .currency("ARS")
                .recipientName("Ana Diaz")
                .phone("555-1234")
                .addressLine("Av. Principal 123")
                .city("Buenos Aires")
                .province("CABA")
                .postalCode("C1000")
                .estimatedFrom(LocalDate.of(2026, 10, 15))
                .estimatedTo(LocalDate.of(2026, 10, 19))
                .createdAt(Instant.parse("2026-10-12T12:00:00Z"))
                .updatedAt(Instant.parse("2026-10-12T12:00:00Z"))
                .build();
        return entity;
    }

    private PaymentApprovedEvent paymentApproved(Long orderId, String customerId) {
        return new PaymentApprovedEvent("PAYMENT_APPROVED", 99L, orderId, "ord-7", customerId,
                new BigDecimal("2499"), "ARS", "MERCADO_PAGO", "prov-1",
                Instant.parse("2026-10-12T12:00:00Z"));
    }

    private OrderCancelledEvent orderCancelled(Long orderId) {
        return new OrderCancelledEvent(orderId, "ord-7", USER, "changed mind", true,
                Instant.parse("2026-10-12T12:00:00Z"));
    }
}
