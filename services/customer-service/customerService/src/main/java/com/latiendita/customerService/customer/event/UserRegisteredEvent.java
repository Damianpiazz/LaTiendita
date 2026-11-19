package com.latiendita.customerService.customer.event;

import java.time.Instant;

public record UserRegisteredEvent(
        String type,
        String sub,
        String email,
        String firstName,
        String lastName,
        Instant occurredAt) {
}
