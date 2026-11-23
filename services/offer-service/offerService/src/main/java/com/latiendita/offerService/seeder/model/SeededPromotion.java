package com.latiendita.offerService.seeder.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Promoción sintética, con las columnas reales de `promotions`. */
public record SeededPromotion(
        UUID id,
        String code,
        String name,
        String type,
        String scope,
        Long productId,
        Long categoryId,
        BigDecimal discountValue,
        BigDecimal minSubtotal,
        Instant startsAt,
        Instant endsAt,
        int priority,
        boolean stackable,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
