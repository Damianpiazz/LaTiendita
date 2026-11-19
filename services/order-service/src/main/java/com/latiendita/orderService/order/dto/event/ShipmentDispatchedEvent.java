package com.latiendita.orderService.order.dto.event;

import java.time.Instant;
import java.time.LocalDate;

public record ShipmentDispatchedEvent(
        String type,
        Long shipmentId,
        Long orderId,
        String customerId,
        Instant dispatchedAt,
        LocalDate estimatedTo,
        Instant occurredAt) {}
