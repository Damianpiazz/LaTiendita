package com.latiendita.cartService.exception;

public class ProductServiceUnavailableException extends RuntimeException {

    public static final String MESSAGE = "Product service is not available";

    public ProductServiceUnavailableException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
