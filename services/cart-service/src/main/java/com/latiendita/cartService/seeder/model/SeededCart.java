package com.latiendita.cartService.seeder.model;

import java.time.Instant;
import java.util.UUID;

/** Carrito sintético, con las columnas reales de `carts`. */
public record SeededCart(
        UUID id,
        String userId,
        Instant createdAt,
        Instant updatedAt,
        long version
) {}
