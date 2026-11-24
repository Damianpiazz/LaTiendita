package com.latiendita.reviewService.exception;

public class NotPurchasedException extends RuntimeException {

    public NotPurchasedException(long productId) {
        super("Only customers who purchased this product can review it (product " + productId + ")");
    }
}
