package com.latiendita.cartService.cart.dto;

import com.latiendita.cartService.exception.InvalidQuantityException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
        @Min(value = 1, message = InvalidQuantityException.MESSAGE)
        @Max(value = 99, message = InvalidQuantityException.MESSAGE)
        int quantity
) {
}
