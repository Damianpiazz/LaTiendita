package com.latiendita.reviewService.seeder.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Resumen de calificaciones, con las columnas reales de `product_ratings`. */
public record SeededProductRating(
        Long productId,
        BigDecimal average,
        int reviewCount,
        String distribution,
        Instant updatedAt
) {}
