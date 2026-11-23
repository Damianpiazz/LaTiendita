package com.latiendita.customerService.newsletter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsletterSubscriptionServiceTest {

    @Mock
    private NewsletterSubscriptionRepository repository;

    @InjectMocks
    private NewsletterSubscriptionService service;

    @Test
    void subscribesNewEmailNormalized() {
        when(repository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(NewsletterSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.subscribe("  Test@Example.com  ");

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.id()).isNotNull();
        assertThat(response.subscribedAt()).isNotNull();
    }

    @Test
    void repeatedSubscriptionKeepsTheOriginalRecord() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var existing = NewsletterSubscription.builder()
                .id(id)
                .email("test@example.com")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
        when(repository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        var response = service.subscribe("TEST@example.com");

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.subscribedAt()).isAfterOrEqualTo(createdAt);
    }
}
