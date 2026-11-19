package com.latiendita.offerService.promotion.dto;

import com.latiendita.offerService.promotion.PromotionScope;
import com.latiendita.offerService.promotion.PromotionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionDto(
        UUID id,
        String code,
        String name,
        PromotionType type,
        PromotionScope scope,
        Long productId,
        Long categoryId,
        BigDecimal discountValue,
        BigDecimal minSubtotal,
        Instant startsAt,
        Instant endsAt,
        Integer priority,
        Boolean stackable,
        Boolean active
) {
}
