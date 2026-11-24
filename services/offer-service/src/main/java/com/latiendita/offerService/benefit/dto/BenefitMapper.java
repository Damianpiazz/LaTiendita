package com.latiendita.offerService.benefit.dto;

import com.latiendita.offerService.benefit.BenefitEntity;

public final class BenefitMapper {

    private BenefitMapper() {}

    public static BenefitDto toDto(BenefitEntity entity) {
        return new BenefitDto(entity.getId(), entity.getCode(), entity.getTitle(),
                entity.getDescription(), entity.getIcon(), entity.getSortOrder());
    }
}
