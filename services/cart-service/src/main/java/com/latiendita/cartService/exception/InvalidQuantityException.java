package com.latiendita.cartService.exception;

public class InvalidQuantityException extends RuntimeException {

    public static final String MESSAGE = "Quantity must be between 1 and 99";

    public InvalidQuantityException() {
        super(MESSAGE);
    }
}
