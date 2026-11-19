package com.latiendita.customerService.customer.event;

import java.time.Instant;
import java.util.UUID;

public record CustomerCreatedEvent(
        String type,
        UUID id,
        String sub,
        String email,
        Instant occurredAt) {
}
