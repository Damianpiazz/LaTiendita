package com.latiendita.reviewService.exception;

public class AdminRoleRequiredException extends RuntimeException {

    public AdminRoleRequiredException() {
        super("Admin role required for review moderation");
    }
}
