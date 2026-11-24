package com.latiendita.offerService.promotion.event;

import com.latiendita.offerService.promotion.PromotionScope;
import java.time.Instant;
import java.util.UUID;

public record PromotionChangedEvent(
        String type,
        UUID promotionId,
        PromotionScope scope,
        Long targetId,
        boolean active,
        Instant occurredAt
) {
}
