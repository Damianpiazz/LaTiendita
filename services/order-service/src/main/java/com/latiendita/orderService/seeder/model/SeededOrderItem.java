package com.latiendita.orderService.seeder.model;

import java.math.BigDecimal;

/** Ítem sintético con FK real a `orders` y `product_id` externo. */
public record SeededOrderItem(
        Long id,
        Long orderId,
        Long productId,
        String productTitle,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
