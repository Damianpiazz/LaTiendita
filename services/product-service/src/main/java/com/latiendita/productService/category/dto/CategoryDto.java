package com.latiendita.productService.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Category contract shared by the catalog, the admin and the Home screen.
 *
 * The first three components keep their original types so existing internal
 * callers remain source-compatible. The Home-only fields are additive; the
 * string `categoryId` is produced at serialization time, not in the Java API.
 */
public record CategoryDto(
        @JsonSerialize(using = ToStringSerializer.class) Long categoryId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        String slug,
        Boolean featured,
        Long productCount
) {

    /** Admin clients may omit derived fields; they never change the stored category. */
    public CategoryDto {
        if (featured == null) {
            featured = false;
        }
        if (productCount == null) {
            productCount = 0L;
        }
    }

    /** Backward-compatible constructor for existing internal callers and tests. */
    public CategoryDto(Long categoryId, String name, String description) {
        this(categoryId, name, description, null, false, 0L);
    }
}
