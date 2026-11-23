package com.latiendita.reviewService.seeder.model;

import java.time.Instant;

/** Evento de infraestructura sintético, marcado `SENT` para no republicarlo. */
public record SeededOutboxEvent(
        String topic,
        String aggregateId,
        String eventType,
        String payload,
        String status,
        int attempts,
        Instant createdAt,
        Instant sentAt
) {}
