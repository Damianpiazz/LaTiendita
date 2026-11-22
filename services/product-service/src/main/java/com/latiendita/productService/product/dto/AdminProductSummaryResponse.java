package com.latiendita.productService.product.dto;

/** Counters shown in the admin products subtitle. */
public record AdminProductSummaryResponse(
        long totalProducts,
        long totalCategories,
        long totalLowStock,
        int lowStockThreshold
) {}
