package com.latiendita.reviewService.review.dto;

import com.latiendita.reviewService.review.ProductRatingEntity;
import com.latiendita.reviewService.review.ReviewEntity;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewDto toDto(ReviewEntity entity) {
        return new ReviewDto(
                entity.getId(),
                entity.getProductId(),
                entity.getCustomerId(),
                entity.getOrderId(),
                entity.getRating(),
                entity.getTitle(),
                entity.getBody(),
                entity.getStatus(),
                entity.getHelpfulVotes(),
                entity.getModerationReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static ReviewSummaryDto toSummary(ProductRatingEntity rating) {
        return new ReviewSummaryDto(
                rating.getProductId(),
                rating.getAverage(),
                rating.getReviewCount(),
                rating.getDistribution());
    }
}
