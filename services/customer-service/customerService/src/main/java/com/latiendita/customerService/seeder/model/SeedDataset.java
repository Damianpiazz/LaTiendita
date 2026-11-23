package com.latiendita.customerService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(List<SeededCustomer> customers, List<SeededOutboxEvent> events) {}
