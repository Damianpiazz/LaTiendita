package com.latiendita.productService.product.dto;

import com.latiendita.productService.product.ProductEntity;

/** Maps catalog entities into the admin table contract, including the derived status. */
public final class AdminProductMapper {

    public static final String STATUS_AGOTADO = "agotado";
    public static final String STATUS_PUBLICADO = "publicado";
    public static final String STATUS_BORRADOR = "borrador";

    private AdminProductMapper() {}

    /**
     * Out of stock wins over the publish flag; otherwise the flag decides between
     * published and draft. A missing quantity is treated as no stock.
     */
    public static String status(Integer quantity, Boolean published) {
        if (quantity == null || quantity <= 0) {
            return STATUS_AGOTADO;
        }
        return Boolean.TRUE.equals(published) ? STATUS_PUBLICADO : STATUS_BORRADOR;
    }

    public static AdminProductResponse toDto(ProductEntity entity, String categoryName) {
        boolean published = Boolean.TRUE.equals(entity.getPublished());
        return new AdminProductResponse(
                entity.getId() == null ? null : entity.getId().toString(),
                entity.getTitle(),
                entity.getSku(),
                categoryName,
                entity.getPrice(),
                entity.getQuantity(),
                status(entity.getQuantity(), published),
                published);
    }
}
