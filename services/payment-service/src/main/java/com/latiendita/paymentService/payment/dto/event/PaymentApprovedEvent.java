package com.latiendita.paymentService.payment.dto.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentApprovedEvent(
        String type,
        Long paymentId,
        Long orderId,
        String orderNumber,
        String customerId,
        BigDecimal amount,
        String currency,
        String provider,
        String providerPaymentId,
        Instant occurredAt) {}
