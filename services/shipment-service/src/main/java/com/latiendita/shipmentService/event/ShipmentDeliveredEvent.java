package com.latiendita.shipmentService.event;

import java.time.Instant;

public record ShipmentDeliveredEvent(
        String type,
        Long shipmentId,
        Long orderId,
        String customerId,
        Instant deliveredAt,
        Instant occurredAt) {
}
