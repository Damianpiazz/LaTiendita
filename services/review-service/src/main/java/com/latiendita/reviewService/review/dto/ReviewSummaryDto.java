package com.latiendita.reviewService.review.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReviewSummaryDto(
        Long productId,
        BigDecimal average,
        long reviewCount,
        Map<String, Integer> distribution
) {
}
