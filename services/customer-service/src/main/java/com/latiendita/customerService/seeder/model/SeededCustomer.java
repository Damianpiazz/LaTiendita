package com.latiendita.customerService.seeder.model;

import java.time.Instant;
import java.util.UUID;

/** Cliente sintético, con todos los campos reales de `customers`. */
public record SeededCustomer(
        UUID id,
        String authorizerSub,
        String email,
        String firstName,
        String lastName,
        String phone,
        String status,
        boolean newsletterOptIn,
        Instant newsletterConsentAt,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {}
