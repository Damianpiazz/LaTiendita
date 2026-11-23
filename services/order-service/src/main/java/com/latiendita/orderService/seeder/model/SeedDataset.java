package com.latiendita.orderService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(
        List<SeededOrder> orders,
        List<SeededOrderItem> items,
        List<SeededOutboxEvent> events
) {}
