package com.latiendita.orderService.exception;

public class AdminRoleRequiredException extends RuntimeException {

    public AdminRoleRequiredException() {
        super("Se requiere el rol 'admin' para esta operación");
    }
}
