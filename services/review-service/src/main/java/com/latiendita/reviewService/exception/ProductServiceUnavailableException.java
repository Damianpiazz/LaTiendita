package com.latiendita.reviewService.exception;

public class ProductServiceUnavailableException extends RuntimeException {

    public ProductServiceUnavailableException() {
        super("Product service is not available");
    }

    public ProductServiceUnavailableException(Throwable cause) {
        super("Product service is not available", cause);
    }
}
