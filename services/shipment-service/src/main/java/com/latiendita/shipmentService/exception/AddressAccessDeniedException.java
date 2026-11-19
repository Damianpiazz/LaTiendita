package com.latiendita.shipmentService.exception;

public class AddressAccessDeniedException extends RuntimeException {

    public AddressAccessDeniedException(Long id) {
        super("No tiene permiso sobre la dirección " + id);
    }
}
