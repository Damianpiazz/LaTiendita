package com.latiendita.offerService.promotion.dto;

import com.latiendita.offerService.promotion.PromotionType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EvaluationDto(
        UUID promotionId,
        PromotionType type,
        BigDecimal discountValue,
        BigDecimal appliedAmount,
        List<EligibleTarget> eligible
) {

    public record EligibleTarget(
            Long targetId,
            BigDecimal discountValue
    ) {
    }
}
