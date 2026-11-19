package com.latiendita.cartService.exception;

public class ProductWithoutPriceException extends RuntimeException {

    public ProductWithoutPriceException(Long productId) {
        super("Product " + productId + " has no price assigned");
    }
}
