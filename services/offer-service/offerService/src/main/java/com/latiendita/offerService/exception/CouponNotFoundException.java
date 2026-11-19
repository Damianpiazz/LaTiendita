package com.latiendita.offerService.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String code) {
        super("Coupon with code " + code + " not found");
    }
}
