package com.latiendita.cartService.cart.event;

import java.math.BigDecimal;

public record ProductPriceChangedEvent(
        String type,
        Long productId,
        BigDecimal price,
        String occurredAt
) {
}
