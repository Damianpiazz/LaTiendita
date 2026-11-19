package com.latiendita.reviewService.exception;

public class InvalidRatingException extends RuntimeException {

    public InvalidRatingException(int rating) {
        super("Rating must be between 1 and 5: " + rating);
    }
}
