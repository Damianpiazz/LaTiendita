package com.latiendita.customerService.newsletter;

import java.time.Instant;
import java.util.UUID;

public record NewsletterSubscriptionResponse(UUID id, String email, Instant subscribedAt) {

    static NewsletterSubscriptionResponse from(NewsletterSubscription subscription) {
        return new NewsletterSubscriptionResponse(
                subscription.getId(), subscription.getEmail(), subscription.getUpdatedAt());
    }
}
