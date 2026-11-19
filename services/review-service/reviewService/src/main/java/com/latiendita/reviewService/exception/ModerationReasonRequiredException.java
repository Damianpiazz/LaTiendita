package com.latiendita.reviewService.exception;

public class ModerationReasonRequiredException extends RuntimeException {

    public ModerationReasonRequiredException() {
        super("A moderation reason is required when rejecting a review");
    }
}
