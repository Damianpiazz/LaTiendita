package com.latiendita.paymentService.payment.dto;

import com.latiendita.paymentService.payment.PaymentStatus;
import com.latiendita.paymentService.provider.ProviderType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Row of the admin payments table.
 *
 * <p>Only fields actually persisted by this service are exposed: the entity
 * holds neither the customer name nor the email, so neither is invented here.
 */
public record AdminPaymentResponse(
        Long id,
        String orderNumber,
        String customerId,
        ProviderType provider,
        String paymentMethodId,
        String paymentType,
        Instant paidAt,
        BigDecimal amount,
        PaymentStatus status
) {}
