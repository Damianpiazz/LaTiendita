package com.latiendita.reviewService.exception;

public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException(long productId) {
        super("Customer already reviewed product " + productId);
    }
}
