package com.latiendita.productService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(List<SeedCategory> categories, List<SeedProduct> products) {}
