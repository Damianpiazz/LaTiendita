package com.latiendita.offerService.seeder.model;

import java.time.Instant;
import java.util.UUID;

/** Uso de cupón sintético, con las columnas reales de `coupon_usages`. */
public record SeededCouponUsage(
        UUID id,
        UUID promotionId,
        String customerSub,
        Long orderId,
        Instant usedAt
) {}
