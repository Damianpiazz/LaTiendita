package com.latiendita.offerService.seeder.model;

import java.time.Instant;
import java.util.UUID;

/** Beneficio sintético, con las columnas reales de `benefits`. */
public record SeededBenefit(
        UUID id,
        String code,
        String title,
        String description,
        String icon,
        int sortOrder,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
