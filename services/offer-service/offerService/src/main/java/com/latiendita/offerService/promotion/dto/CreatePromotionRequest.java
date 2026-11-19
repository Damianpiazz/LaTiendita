package com.latiendita.offerService.promotion.dto;

import com.latiendita.offerService.promotion.PromotionScope;
import com.latiendita.offerService.promotion.PromotionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record CreatePromotionRequest(
        @Size(max = 50) String code,
        @NotBlank @Size(max = 120) String name,
        @NotNull PromotionType type,
        @NotNull PromotionScope scope,
        Long productId,
        Long categoryId,
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 10, fraction = 2) BigDecimal discountValue,
        @DecimalMin(value = "0.0") @Digits(integer = 10, fraction = 2) BigDecimal minSubtotal,
        @NotNull Instant startsAt,
        Instant endsAt,
        @NotNull Integer priority,
        @NotNull Boolean stackable,
        @NotNull Boolean active
) {
}
