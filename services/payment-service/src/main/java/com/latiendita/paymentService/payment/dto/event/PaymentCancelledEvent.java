package com.latiendita.paymentService.payment.dto.event;

import java.time.Instant;

public record PaymentCancelledEvent(
        String type,
        Long paymentId,
        Long orderId,
        String customerId,
        Instant occurredAt) {}
