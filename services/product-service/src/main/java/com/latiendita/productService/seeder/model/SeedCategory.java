package com.latiendita.productService.seeder.model;

/**
 * Fila de categoría generada por el seeder, ya con el ID sintético asignado.
 * Los IDs se reservan en Java para que las relaciones sean deterministas y no
 * dependan del orden en que PostgreSQL asigne valores de identidad.
 */
public record SeedCategory(
        long id,
        String name,
        String slug,
        String description,
        boolean active,
        boolean featured,
        String imageUrl
) {}
