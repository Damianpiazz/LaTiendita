package com.latiendita.shipmentService.seeder.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Envío sintético con las mismas columnas que `shipments`. */
public record SeededShipment(
        long id,
        long orderId,
        String customerId,
        String status,
        BigDecimal shippingCost,
        String currency,
        String recipientName,
        String phone,
        String addressLine,
        String city,
        String province,
        String postalCode,
        String references,
        LocalDate estimatedFrom,
        LocalDate estimatedTo,
        Instant dispatchedAt,
        Instant deliveredAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {}
