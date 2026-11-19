package com.latiendita.cartService.exception;

public class MissingGatewayHeaderException extends RuntimeException {

    public MissingGatewayHeaderException() {
        super("Missing gateway identity header");
    }
}
