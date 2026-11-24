package com.latiendita.reviewService.exception;

import java.util.UUID;

public class DuplicateVoteException extends RuntimeException {

    public DuplicateVoteException(UUID reviewId) {
        super("Customer already voted review " + reviewId + " as helpful");
    }
}
