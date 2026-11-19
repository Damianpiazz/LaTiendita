package com.latiendita.cartService.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(int requested, int stock) {
        super("Requested quantity " + requested + " exceeds available stock " + stock);
    }
}
