package com.latiendita.paymentService.exception;

public class AdminRoleRequiredException extends RuntimeException {

    public AdminRoleRequiredException() {
        super("Se requiere el rol 'admin' para esta operación");
    }
}
