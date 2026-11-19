package com.latiendita.offerService.exception;

public class DuplicatePromotionCodeException extends RuntimeException {

    public DuplicatePromotionCodeException(String code) {
        super("Promotion code " + code + " already exists");
    }
}
