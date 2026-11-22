package com.latiendita.productService.product.dto;

import java.math.BigDecimal;

/** Row of the admin products table. `status` is derived, never persisted. */
public record AdminProductResponse(
        String productId,
        String name,
        String sku,
        String category,
        BigDecimal price,
        Integer stock,
        String status,
        boolean published
) {}
