package com.latiendita.cartService.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long productId) {
        super("Cart item for product " + productId + " not found");
    }
}
