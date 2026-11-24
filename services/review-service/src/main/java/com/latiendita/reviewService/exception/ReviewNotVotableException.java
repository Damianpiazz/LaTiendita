package com.latiendita.reviewService.exception;

import java.util.UUID;

public class ReviewNotVotableException extends RuntimeException {

    public ReviewNotVotableException(UUID reviewId) {
        super("Review " + reviewId + " is not approved and cannot be voted as helpful");
    }
}
