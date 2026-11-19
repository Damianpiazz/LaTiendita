package com.latiendita.shipmentService.exception;

public class DuplicateShipmentException extends RuntimeException {

    public DuplicateShipmentException(Long orderId) {
        super("Ya existe un envío para el pedido " + orderId);
    }
}
