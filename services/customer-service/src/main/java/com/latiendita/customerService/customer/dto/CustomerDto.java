package com.latiendita.customerService.customer.dto;

import com.latiendita.customerService.customer.CustomerStatus;
import java.time.Instant;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String authorizerSub,
        String email,
        String firstName,
        String lastName,
        String phone,
        CustomerStatus status,
        boolean newsletterOptIn,
        Instant newsletterConsentAt,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt) {
}
