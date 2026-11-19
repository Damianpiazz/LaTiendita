package com.latiendita.reviewService.exception;

public class MissingGatewayHeaderException extends RuntimeException {

    public MissingGatewayHeaderException() {
        super("Missing X-User-Id gateway header");
    }
}
