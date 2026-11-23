package com.latiendita.productService.seeder.model;

import java.math.BigDecimal;

/** Producto generado por el seeder con sus columnas JSON ya serializadas. */
public record SeedProduct(
        long id,
        String title,
        String imageUrl,
        String sku,
        BigDecimal price,
        int quantity,
        long categoryId,
        String slug,
        String subcategory,
        boolean featured,
        boolean published,
        BigDecimal originalPrice,
        String fulfillment,
        String imagesJson,
        String descriptionJson,
        String highlightsJson,
        String specsJson
) {}
