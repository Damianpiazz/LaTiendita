package com.latiendita.cartService.product;

import java.math.BigDecimal;

/**
 * Server-side facts the cart is allowed to persist. Price, title and stock come
 * only from product-service, never from a client-supplied request body.
 */
public record ProductSnapshot(
        Long productId,
        String title,
        String imageUrl,
        BigDecimal price,
        int stock
) {
}
