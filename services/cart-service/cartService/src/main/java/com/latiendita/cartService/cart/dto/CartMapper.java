package com.latiendita.cartService.cart.dto;

import com.latiendita.cartService.cart.CartEntity;
import com.latiendita.cartService.cart.CartItemEntity;
import java.math.BigDecimal;
import java.util.List;

public final class CartMapper {

    private CartMapper() {}

    /**
     * All totals are derived from the persisted quantity and price snapshot;
     * none of them is trusted from a request. `itemCount` is the number of units
     * (sum of quantities) so it can drive the header badge, and `total` equals
     * the subtotal because shipping is resolved by shipment-service at checkout.
     */
    public static CartDto toDto(CartEntity cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(CartMapper::toItemDto)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemDto::lineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = items.stream().mapToInt(CartItemDto::quantity).sum();

        return new CartDto(cart.getId(), items, subtotal, itemCount, subtotal);
    }

    public static CartDto emptyDto() {
        return new CartDto(null, List.of(), BigDecimal.ZERO, 0, BigDecimal.ZERO);
    }

    public static CartItemDto toItemDto(CartItemEntity item) {
        BigDecimal lineSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemDto(
                item.getProductId(),
                item.getProductTitle(),
                item.getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineSubtotal,
                item.isPriceChanged());
    }
}
