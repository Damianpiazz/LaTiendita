package com.latiendita.reviewService.exception;

public class OrderServiceUnavailableException extends RuntimeException {

    public OrderServiceUnavailableException() {
        super("Order service is not available");
    }

    public OrderServiceUnavailableException(Throwable cause) {
        super("Order service is not available", cause);
    }
}
