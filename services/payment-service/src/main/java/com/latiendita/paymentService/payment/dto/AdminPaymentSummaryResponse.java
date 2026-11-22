package com.latiendita.paymentService.payment.dto;

import java.math.BigDecimal;

/**
 * Payment-owned KPIs of the admin dashboard.
 *
 * <p>{@code paymentsToClear} and {@code paymentsInReview} keep their original
 * all-time semantics because Admin Resumen consumes them. The remaining KPIs
 * are scoped to the requested {@code rangeDays} window.
 */
public record AdminPaymentSummaryResponse(
        BigDecimal paymentsToClear,
        long paymentsInReview,
        long totalPayments,
        long approvedCount,
        long pendingCount,
        long rejectedCount,
        BigDecimal approvedVolume,
        BigDecimal pendingVolume,
        BigDecimal rejectedVolume,
        int approvalRate,
        long approvalAttempts,
        int rangeDays
) {}
