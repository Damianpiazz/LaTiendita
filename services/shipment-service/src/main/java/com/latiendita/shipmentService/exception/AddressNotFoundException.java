package com.latiendita.shipmentService.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long id) {
        super("Dirección no encontrada con id " + id);
    }
}
