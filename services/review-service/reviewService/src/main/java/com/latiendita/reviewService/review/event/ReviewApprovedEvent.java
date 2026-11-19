package com.latiendita.reviewService.review.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReviewApprovedEvent(
        String type,
        UUID reviewId,
        Long productId,
        int rating,
        BigDecimal average,
        int reviewCount,
        Instant occurredAt
) {
}
