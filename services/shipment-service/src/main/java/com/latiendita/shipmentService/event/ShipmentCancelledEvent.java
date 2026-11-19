package com.latiendita.shipmentService.event;

import java.time.Instant;

public record ShipmentCancelledEvent(
        String type,
        Long shipmentId,
        Long orderId,
        String customerId,
        Instant occurredAt) {
}
