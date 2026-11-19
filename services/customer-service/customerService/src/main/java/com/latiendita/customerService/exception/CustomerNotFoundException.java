package com.latiendita.customerService.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID id) {
        super("Customer with id " + id + " not found");
    }

    public CustomerNotFoundException(String sub) {
        super("Customer with sub " + sub + " not found");
    }
}
