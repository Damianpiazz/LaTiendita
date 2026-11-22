package com.latiendita.productService.category.dto;

import com.latiendita.productService.category.CategoryEntity;
import java.text.Normalizer;
import java.util.Locale;

public final class CategoryMapper {

    private CategoryMapper() {}

    /** Kept for callers that do not need product counts. */
    public static CategoryDto toDto(CategoryEntity entity) {
        return toDto(entity, 0L, 0L);
    }

    /** Kept for callers that do not need a low-stock count. */
    public static CategoryDto toDto(CategoryEntity entity, long productCount) {
        return toDto(entity, productCount, 0L);
    }

    public static CategoryDto toDto(CategoryEntity entity, long productCount, long lowStockCount) {
        return new CategoryDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                resolveSlug(entity),
                entity.isFeatured(),
                productCount,
                entity.isActive(),
                entity.getImageUrl(),
                lowStockCount);
    }

    /** The stored slug wins; legacy rows with a null slug use the derived one. */
    public static String resolveSlug(CategoryEntity entity) {
        String stored = entity.getSlug();
        return stored != null && !stored.isBlank() ? stored : slugify(entity.getName());
    }

    /** Derives a URL-safe slug from a category name, products' convention. */
    public static String slugify(String name) {
        if (name == null || name.isBlank()) {
            return "categoria";
        }
        String slug = Normalizer.normalize(name, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "categoria" : slug;
    }
}
