package com.latiendita.customerService.exception;

public class MissingGatewayHeaderException extends RuntimeException {

    public MissingGatewayHeaderException() {
        super("Missing or invalid X-User-Id header; the request must pass through the gateway");
    }
}
