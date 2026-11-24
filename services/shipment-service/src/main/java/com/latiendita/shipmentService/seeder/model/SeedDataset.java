package com.latiendita.shipmentService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(
        List<SeededShipment> shipments,
        List<SeededAddress> addresses,
        List<SeededOutboxEvent> events
) {}
