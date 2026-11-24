package com.latiendita.shipmentService.seeder.model;

import java.time.Instant;

/** Evento de infraestructura ya enviado, para no disparar republicaciones. */
public record SeededOutboxEvent(
        String topic,
        String aggregateId,
        String eventType,
        String payload,
        Instant createdAt,
        Instant sentAt
) {}
