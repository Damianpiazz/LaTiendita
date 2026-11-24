package com.latiendita.offerService.coupon.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CouponDto(
        String code,
        UUID promotionId,
        Boolean valid,
        BigDecimal discountValue,
        BigDecimal minSubtotal,
        Instant startsAt,
        Instant endsAt,
        Long usageCount
) {
}
