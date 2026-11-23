package com.latiendita.paymentService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(
        List<SeededPayment> payments,
        List<SeededPaymentEvent> events,
        List<SeededOutboxEvent> outbox
) {}
