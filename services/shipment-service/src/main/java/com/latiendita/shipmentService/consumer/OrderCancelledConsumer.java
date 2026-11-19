package com.latiendita.shipmentService.consumer;

import com.latiendita.shipmentService.consumer.event.OrderCancelledEvent;
import com.latiendita.shipmentService.shipment.ShipmentService;
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
public class OrderCancelledConsumer {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = ORDER_EVENTS_TOPIC)
    @Transactional
    public void onOrderEvent(String payload) {
        Map<String, Object> node;
        try {
            node = objectMapper.readValue(payload, Map.class);
        } catch (Exception ex) {
            log.warn("Mensaje malformado en order-events descartado: {}", payload);
            return;
        }

        if (!isCancellation(node)) {
            return;
        }

        OrderCancelledEvent event;
        try {
            event = objectMapper.readValue(payload, OrderCancelledEvent.class);
        } catch (Exception ex) {
            log.warn("Evento ORDER_CANCELLED malformado descartado: {}", payload);
            return;
        }
        shipmentService.cancelForOrder(event);
    }

    /**
     * order-service publishes ORDER_CANCELLED without a {@code type} field, so the
     * distinctive cancellation fields also identify the event.
     */
    private boolean isCancellation(Map<String, Object> node) {
        Object type = node.get("type");
        if (type != null) {
            return ORDER_CANCELLED.equals(type.toString());
        }
        return node.get("orderId") != null
                && (node.get("reason") != null || node.get("refundRequired") != null);
    }
}
