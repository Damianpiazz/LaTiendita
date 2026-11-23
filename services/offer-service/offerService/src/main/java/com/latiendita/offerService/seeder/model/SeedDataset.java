package com.latiendita.offerService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(
        List<SeededPromotion> promotions,
        List<SeededCouponUsage> usages,
        List<SeededBenefit> benefits,
        List<SeededOutboxEvent> events
) {}
