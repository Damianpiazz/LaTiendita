package com.latiendita.productService.category.dto;

import com.latiendita.productService.category.CategoryEntity;
import java.text.Normalizer;
import java.util.Locale;

public final class CategoryMapper {

    private CategoryMapper() {}

    /** Kept for callers that do not need a product count. */
    public static CategoryDto toDto(CategoryEntity entity) {
        return toDto(entity, 0L);
    }

    public static CategoryDto toDto(CategoryEntity entity, long productCount) {
        return new CategoryDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                slugify(entity.getName()),
                entity.isFeatured(),
                productCount);
    }

    /** Categories have no stored slug yet: derive it the same way product slugs are derived. */
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
