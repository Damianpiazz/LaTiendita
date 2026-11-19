package com.latiendita.orderService.order.shipment;

import com.latiendita.orderService.order.OrderEntity;
import com.latiendita.orderService.order.OrderRepository;
import com.latiendita.orderService.order.OrderStatus;
import com.latiendita.orderService.order.dto.event.ShipmentDispatchedEvent;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentDispatchedConsumer {

    public static final String SHIPMENT_EVENTS_TOPIC = "shipment-events";
    public static final String SHIPMENT_DISPATCHED = "SHIPMENT_DISPATCHED";

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = SHIPMENT_EVENTS_TOPIC)
    @Transactional
    public void onShipmentEvent(String payload) {
        String type = readEventType(payload);
        if (!SHIPMENT_DISPATCHED.equals(type)) {
            return;
        }

        ShipmentDispatchedEvent event;
        try {
            event = objectMapper.readValue(payload, ShipmentDispatchedEvent.class);
        } catch (Exception ex) {
            log.warn("Evento SHIPMENT_DISPATCHED malformado descartado: {}", payload);
            return;
        }

        markShipped(event);
    }

    private String readEventType(String payload) {
        try {
            Map<?, ?> node = objectMapper.readValue(payload, Map.class);
            Object type = node.get("type");
            return type == null ? null : type.toString();
        } catch (Exception ex) {
            log.warn("Mensaje malformado en shipment-events descartado: {}", payload);
            return null;
        }
    }

    private void markShipped(ShipmentDispatchedEvent event) {
        OrderEntity order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Se recibió SHIPMENT_DISPATCHED para el pedido inexistente "
                                + event.orderId()));

        if (order.getStatus() != OrderStatus.PAID) {
            log.info("Despacho para el pedido {} recibido en estado {}; se ignora (idempotente)",
                    order.getId(), order.getStatus());
            return;
        }

        log.info("Pedido {} pasa de PAID a SHIPPED por el envío {}",
                order.getId(), event.shipmentId());

        order.setStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }
}
