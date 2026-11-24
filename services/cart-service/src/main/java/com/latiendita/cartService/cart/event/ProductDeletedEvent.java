package com.latiendita.cartService.cart.event;

public record ProductDeletedEvent(
        String type,
        Long productId,
        String occurredAt
) {
}
