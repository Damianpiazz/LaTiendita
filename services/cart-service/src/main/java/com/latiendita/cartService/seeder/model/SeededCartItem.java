package com.latiendita.cartService.seeder.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Ítem sintético, con las columnas reales de `cart_items`. */
public record SeededCartItem(
        UUID id,
        UUID cartId,
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        String productTitle,
        String imageUrl,
        boolean priceChanged,
        Instant createdAt,
        Instant updatedAt
) {}
