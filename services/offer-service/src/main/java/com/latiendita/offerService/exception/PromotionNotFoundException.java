package com.latiendita.offerService.exception;

import java.util.UUID;

public class PromotionNotFoundException extends RuntimeException {

    public PromotionNotFoundException(UUID id) {
        super("Promotion with id " + id + " not found");
    }
}
