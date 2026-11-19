package com.latiendita.productService.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Catalog and product-detail contract.
 *
 * The original fields (productId, title, imageUrl, sku, price, quantity,
 * categoryId) stay unchanged for internal clients. The new fields are owned by
 * this service; ratings, reviews and installments deliberately stay out.
 */
public record ProductDto(
        Long productId,
        @Size(max = 255) String title,
        @Size(max = 500) String imageUrl,
        @NotBlank @Size(max = 100) String sku,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
        @Min(0) Integer quantity,
        Long categoryId,
        @Size(max = 255) String slug,
        @Size(max = 255) String categoryName,
        @Size(max = 100) String subcategory,
        boolean featured,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal originalPrice,
        @Size(max = 100) String fulfillment,
        @Size(max = 10) List<@Size(max = 500) String> images,
        @Size(max = 20) List<@Size(max = 2000) String> description,
        @Size(max = 20) List<@Size(max = 500) String> highlights,
        @Size(max = 30) @Valid List<ProductSpec> specs
) {

    /** Backward-compatible constructor for existing internal callers. */
    public ProductDto(Long productId, String title, String imageUrl, String sku,
                      BigDecimal price, Integer quantity, Long categoryId) {
        this(productId, title, imageUrl, sku, price, quantity, categoryId,
                null, null, null, false, null, null,
                List.of(), List.of(), List.of(), List.of());
    }

    /** Stable id alias consumed by the catalog screens. */
    public String getId() {
        return productId == null ? null : productId.toString();
    }

    /** Name alias consumed by the catalog screens; title remains the stored field. */
    public String getName() {
        return title;
    }

    /** Stock alias consumed by the detail screen; quantity remains the stored field. */
    public Integer getStock() {
        return quantity;
    }

    /** Number of gallery entries exposed by the JSON image list. */
    public int getGalleryCount() {
        return images == null ? 0 : images.size();
    }
}
