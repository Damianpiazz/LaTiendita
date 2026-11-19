package com.latiendita.offerService.consumer.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String type,
        Long orderId,
        String orderNumber,
        String customerId,
        BigDecimal total,
        String couponCode,
        Instant occurredAt
) {
}
