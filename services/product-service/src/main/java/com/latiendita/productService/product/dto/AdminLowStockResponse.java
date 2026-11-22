package com.latiendita.productService.product.dto;

import java.util.List;

public record AdminLowStockResponse(
        List<AdminLowStockProductResponse> items,
        long totalLowStock,
        int threshold
) {}
