package com.latiendita.customerService.customer.dto;

import com.latiendita.customerService.customer.CustomerStatus;
import java.time.Instant;
import java.util.UUID;

public record CustomerSummaryDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        CustomerStatus status,
        Instant lastLoginAt) {
}
