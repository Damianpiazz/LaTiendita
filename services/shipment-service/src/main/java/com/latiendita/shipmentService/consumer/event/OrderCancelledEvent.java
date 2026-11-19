package com.latiendita.shipmentService.consumer.event;

import java.time.Instant;

public record OrderCancelledEvent(
        Long orderId,
        String orderNumber,
        String customerId,
        String reason,
        boolean refundRequired,
        Instant occurredAt) {
}
