package com.latiendita.reviewService.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
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
) {
}
