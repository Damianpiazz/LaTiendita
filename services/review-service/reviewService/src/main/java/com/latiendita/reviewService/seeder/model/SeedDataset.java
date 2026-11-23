package com.latiendita.reviewService.seeder.model;

import java.util.List;

/** Dataset completo y reproducible de una ejecución del seeder. */
public record SeedDataset(
        List<SeededReview> reviews,
        List<SeededVote> votes,
        List<SeededProductRating> ratings,
        List<SeededOutboxEvent> events
) {}
