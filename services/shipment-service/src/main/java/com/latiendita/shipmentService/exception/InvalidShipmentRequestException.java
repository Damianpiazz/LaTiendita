package com.latiendita.shipmentService.exception;

public class InvalidShipmentRequestException extends RuntimeException {

    public InvalidShipmentRequestException(String message) {
        super(message);
    }
}
