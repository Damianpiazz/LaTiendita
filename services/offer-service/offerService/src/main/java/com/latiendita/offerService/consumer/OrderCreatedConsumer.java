package com.latiendita.offerService.consumer;

import com.latiendita.offerService.consumer.event.OrderCreatedEvent;
import com.latiendita.offerService.coupon.CouponService;
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
public class OrderCreatedConsumer {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String ORDER_CREATED = "ORDER_CREATED";

    private final CouponService couponService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = ORDER_EVENTS_TOPIC, groupId = "offer-service")
    @Transactional
    public void onOrderEvent(String payload) {
        String type = readEventType(payload);
        if (!ORDER_CREATED.equals(type)) {
            return;
        }

        OrderCreatedEvent event;
        try {
            event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        } catch (Exception ex) {
            log.warn("Malformed ORDER_CREATED event discarded: {}", payload);
            return;
        }

        // order-service still has to add couponCode to ORDER_CREATED; without it
        // there is no promotion to attribute the usage to.
        if (event.couponCode() == null || event.couponCode().isBlank()) {
            return;
        }

        couponService.recordUsageForCode(event.couponCode(), event.customerId(), event.orderId());
    }

    private String readEventType(String payload) {
        try {
            Map<?, ?> node = objectMapper.readValue(payload, Map.class);
            Object type = node.get("type");
            return type == null ? null : type.toString();
        } catch (Exception ex) {
            log.warn("Malformed message in order-events discarded: {}", payload);
            return null;
        }
    }
}
