package com.latiendita.shipmentService.seeder.model;

import java.time.Instant;

/** Dirección sintética con las mismas columnas que `delivery_addresses`. */
public record SeededAddress(
        long id,
        String customerId,
        String recipientName,
        String phone,
        String addressLine,
        String city,
        String province,
        String postalCode,
        String references,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {}
