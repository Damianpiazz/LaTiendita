package com.latiendita.productService.product.dto;

public record AdminLowStockProductResponse(
        String name,
        String sku,
        int quantity
) {}
