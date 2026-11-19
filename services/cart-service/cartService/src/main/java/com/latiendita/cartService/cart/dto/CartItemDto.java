package com.latiendita.cartService.cart.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Long productId,
        String productTitle,
        String imageUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        boolean priceChanged
) {
}
