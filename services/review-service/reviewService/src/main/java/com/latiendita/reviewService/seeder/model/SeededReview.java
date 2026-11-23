package com.latiendita.reviewService.seeder.model;

import java.time.Instant;
import java.util.UUID;

/** Reseña sintética, con las columnas reales de `reviews`. */
public record SeededReview(
        UUID id,
        Long productId,
        String customerId,
        Long orderId,
        int rating,
        String title,
        String body,
        String status,
        int helpfulVotes,
        String moderationReason,
        Instant createdAt,
        Instant updatedAt
) {}
