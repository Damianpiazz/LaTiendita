package com.latiendita.shipmentService.exception;

public class ShipmentAccessDeniedException extends RuntimeException {

    public ShipmentAccessDeniedException(Long id) {
        super("No tiene permiso sobre el envío " + id);
    }
}
