package com.latiendita.cartService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(
        List<SeededCart> carts,
        List<SeededCartItem> items
) {}
