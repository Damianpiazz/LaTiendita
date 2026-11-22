package com.latiendita.productService.product.dto;

import com.latiendita.productService.product.ProductEntity;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductDto toDto(ProductEntity entity) {
        return toDto(entity, null);
    }

    public static ProductDto toDto(ProductEntity entity, String categoryName) {
        List<String> images = entity.getImages();
        if ((images == null || images.isEmpty()) && hasText(entity.getImageUrl())) {
            images = List.of(entity.getImageUrl());
        }
        return new ProductDto(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getSku(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getCategoryId(),
                entity.getSlug(),
                categoryName,
                entity.getSubcategory(),
                Boolean.TRUE.equals(entity.getFeatured()),
                entity.getOriginalPrice(),
                entity.getFulfillment(),
                images == null ? List.of() : images,
                entity.getDescription() == null ? List.of() : entity.getDescription(),
                entity.getHighlights() == null ? List.of() : entity.getHighlights(),
                entity.getSpecs() == null ? List.of() : entity.getSpecs(),
                entity.getPublished());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
