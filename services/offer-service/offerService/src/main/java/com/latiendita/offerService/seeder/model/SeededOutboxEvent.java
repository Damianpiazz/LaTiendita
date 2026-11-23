package com.latiendita.offerService.seeder.model;

import java.time.Instant;

/**
 * Evento de infraestructura sintético. No incluye `id` porque `outbox_events.id`
 * es identity; los eventos se marcan `SENT` para no republicarlos.
 */
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
