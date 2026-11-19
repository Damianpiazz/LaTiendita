package com.latiendita.shipmentService.exception;

public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(Long id) {
        super("Envío no encontrado con id " + id);
    }

    public ShipmentNotFoundException(Long id, boolean byOrder) {
        super(byOrder
                ? "Envío no encontrado para el pedido " + id
                : "Envío no encontrado con id " + id);
    }
}
