package com.latiendita.reviewService.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModerateReviewRequest(
        @NotBlank
        @Pattern(regexp = "approved|rejected", message = "status must be approved or rejected")
        String status,
        @Size(max = 500) String reason
) {
}
