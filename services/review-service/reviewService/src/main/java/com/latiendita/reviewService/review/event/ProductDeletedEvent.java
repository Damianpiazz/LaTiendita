package com.latiendita.reviewService.review.event;

import java.time.Instant;

public record ProductDeletedEvent(
        String type,
        Long productId,
        Instant occurredAt
) {
}
