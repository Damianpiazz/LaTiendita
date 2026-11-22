package com.latiendita.productService.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Category contract shared by the catalog, the admin and the Home screen.
 *
 * The first three components keep their original types so existing internal
 * callers remain source-compatible. The remaining fields are additive: the
 * string `categoryId` is produced at serialization time, not in the Java API,
 * and `active`/`imageUrl`/`lowStockCount` feed the Admin Categorias screen
 * without changing the meaning of the Home-only `featured` flag.
 */
public record CategoryDto(
        @JsonSerialize(using = ToStringSerializer.class) Long categoryId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        String slug,
        Boolean featured,
        Long productCount,
        Boolean active,
        @Size(max = 500) String imageUrl,
        Long lowStockCount
) {

    /** Clients may omit derived or additive fields; they get safe defaults. */
    public CategoryDto {
        if (featured == null) {
            featured = false;
        }
        if (productCount == null) {
            productCount = 0L;
        }
        if (active == null) {
            active = true;
        }
        if (lowStockCount == null) {
            lowStockCount = 0L;
        }
    }

    /** Backward-compatible constructor for existing internal callers and tests. */
    public CategoryDto(Long categoryId, String name, String description) {
        this(categoryId, name, description, null, false, 0L, true, null, 0L);
    }
}
