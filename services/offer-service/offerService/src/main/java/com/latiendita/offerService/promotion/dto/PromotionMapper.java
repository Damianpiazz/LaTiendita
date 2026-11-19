package com.latiendita.offerService.promotion.dto;

import com.latiendita.offerService.promotion.PromotionEntity;

public final class PromotionMapper {

    private PromotionMapper() {}

    public static PromotionDto toDto(PromotionEntity entity) {
        return new PromotionDto(entity.getId(), entity.getCode(), entity.getName(),
                entity.getType(), entity.getScope(), entity.getProductId(),
                entity.getCategoryId(), entity.getDiscountValue(), entity.getMinSubtotal(),
                entity.getStartsAt(), entity.getEndsAt(), entity.getPriority(),
                entity.isStackable(), entity.isActive());
    }
}
