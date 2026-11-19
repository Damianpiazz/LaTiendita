package com.latiendita.offerService.benefit.dto;

import java.util.UUID;

public record BenefitDto(
        UUID id,
        String code,
        String title,
        String description,
        String icon,
        Integer sortOrder
) {
}
