package com.latiendita.paymentService.seeder.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Pago sintético, con las columnas reales de `payments`. */
public record SeededPayment(
        Long id,
        Long orderId,
        String customerId,
        String provider,
        String status,
        BigDecimal amount,
        String currency,
        String preferenceId,
        String checkoutUrl,
        String externalReference,
        String idempotencyKey,
        String providerPaymentId,
        String paymentMethodId,
        String paymentType,
        String statusDetail,
        Instant paidAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {}
