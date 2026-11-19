package com.latiendita.reviewService.exception;

import java.util.UUID;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(UUID reviewId) {
        super("Review with id " + reviewId + " not found");
    }
}
