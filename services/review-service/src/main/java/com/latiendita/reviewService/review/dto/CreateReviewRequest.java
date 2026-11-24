package com.latiendita.reviewService.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull Long productId,
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 4000) String body
) {
}
