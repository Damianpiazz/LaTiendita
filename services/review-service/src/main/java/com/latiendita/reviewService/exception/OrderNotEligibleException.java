package com.latiendita.reviewService.exception;

public class OrderNotEligibleException extends RuntimeException {

    public OrderNotEligibleException(long orderId, String status) {
        super("Order " + orderId + " is not eligible for review: status " + status);
    }
}
