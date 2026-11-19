package com.latiendita.shipmentService.exception;

public class OrderServiceUnavailableException extends RuntimeException {

    public OrderServiceUnavailableException(Throwable cause) {
        super("El servicio de pedidos no está disponible", cause);
    }
}
