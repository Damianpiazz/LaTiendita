package com.latiendita.shipmentService.consumer;

import com.latiendita.shipmentService.consumer.event.PaymentApprovedEvent;
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
public class PaymentApprovedConsumer {

    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    public static final String PAYMENT_APPROVED = "PAYMENT_APPROVED";

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PAYMENT_EVENTS_TOPIC)
    @Transactional
    public void onPaymentEvent(String payload) {
        if (!PAYMENT_APPROVED.equals(readEventType(payload))) {
            return;
        }

        PaymentApprovedEvent event;
        try {
            event = objectMapper.readValue(payload, PaymentApprovedEvent.class);
        } catch (Exception ex) {
            log.warn("Evento PAYMENT_APPROVED malformado descartado: {}", payload);
            return;
        }
        shipmentService.approvePayment(event);
    }

    private String readEventType(String payload) {
        try {
            Map<?, ?> node = objectMapper.readValue(payload, Map.class);
            Object type = node.get("type");
            return type == null ? null : type.toString();
        } catch (Exception ex) {
            log.warn("Mensaje malformado en payment-events descartado: {}", payload);
            return null;
        }
    }
}
