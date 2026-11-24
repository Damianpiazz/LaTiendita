package com.latiendita.reviewService.exception;

public class SelfVoteException extends RuntimeException {

    public SelfVoteException() {
        super("Authors cannot vote their own review as helpful");
    }
}
