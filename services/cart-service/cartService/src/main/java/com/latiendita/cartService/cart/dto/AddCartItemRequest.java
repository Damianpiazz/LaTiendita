package com.latiendita.cartService.cart.dto;

import com.latiendita.cartService.exception.InvalidQuantityException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull Long productId,
        @Min(value = 1, message = InvalidQuantityException.MESSAGE)
        @Max(value = 99, message = InvalidQuantityException.MESSAGE)
        int quantity
) {
}
