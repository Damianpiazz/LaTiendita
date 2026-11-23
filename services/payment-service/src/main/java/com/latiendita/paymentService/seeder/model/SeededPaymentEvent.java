package com.latiendita.paymentService.seeder.model;

import java.time.Instant;

/** Notificación de webhook ya procesada (inbox de idempotencia). */
public record SeededPaymentEvent(
        String provider,
        String notificationId,
        String resourceType,
        String resourceId,
        String action,
        boolean signatureValid,
        String status,
        int attempts,
        String lastError,
        Instant createdAt,
        Instant processedAt
) {}
