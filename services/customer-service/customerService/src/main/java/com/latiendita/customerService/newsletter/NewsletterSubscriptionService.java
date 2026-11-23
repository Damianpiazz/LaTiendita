package com.latiendita.customerService.newsletter;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionService {

    private final NewsletterSubscriptionRepository repository;

    /** Idempotent: repeated submissions refresh the timestamp without failing. */
    @Transactional
    public NewsletterSubscriptionResponse subscribe(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        Instant now = Instant.now();
        NewsletterSubscription subscription = repository.findByEmailIgnoreCase(email)
                .orElseGet(() -> NewsletterSubscription.builder()
                        .id(UUID.randomUUID())
                        .email(email)
                        .createdAt(now)
                        .build());

        subscription.setUpdatedAt(now);
        return NewsletterSubscriptionResponse.from(repository.save(subscription));
    }
}
