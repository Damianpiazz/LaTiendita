package com.latiendita.reviewService.review.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReviewStatusChangedEvent(
        String type,
        UUID reviewId,
        Long productId,
        String status,
        BigDecimal average,
        int reviewCount,
        Instant occurredAt
) {
}
