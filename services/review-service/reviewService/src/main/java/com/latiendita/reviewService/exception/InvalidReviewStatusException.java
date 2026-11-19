package com.latiendita.reviewService.exception;

public class InvalidReviewStatusException extends RuntimeException {

    public InvalidReviewStatusException(String from, String to) {
        super("Invalid review status transition: " + from + " -> " + to);
    }
}
