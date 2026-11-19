package com.latiendita.reviewService.review;

public final class ReviewStatus {

    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";

    private ReviewStatus() {
    }

    public static boolean isValid(String status) {
        return PENDING.equals(status) || APPROVED.equals(status) || REJECTED.equals(status);
    }
}
