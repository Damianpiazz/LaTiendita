package com.latiendita.reviewService.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(long productId) {
        super("Product with id " + productId + " not found");
    }
}
