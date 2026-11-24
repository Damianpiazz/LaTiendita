package com.latiendita.reviewService.seeder.model;

import java.time.Instant;
import java.util.UUID;

/** Voto útil sintético, con las columnas reales de `review_votes`. */
public record SeededVote(
        UUID id,
        UUID reviewId,
        String customerId,
        Instant createdAt
) {}
