package com.latiendita.paymentService.payment.dto;

import com.latiendita.paymentService.payment.PaymentEntity;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(PaymentEntity entity) {
        return new PaymentResponse(
                entity.getId(),
                entity.getOrderId(),
                entity.getExternalReference(),
                entity.getCustomerId(),
                entity.getProvider(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getPreferenceId(),
                entity.getCheckoutUrl(),
                entity.getPaymentMethodId(),
                entity.getPaymentType(),
                entity.getStatusDetail(),
                entity.getPaidAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static AdminPaymentResponse toAdminResponse(PaymentEntity entity) {
        return new AdminPaymentResponse(
                entity.getId(),
                entity.getExternalReference(),
                entity.getCustomerId(),
                entity.getProvider(),
                entity.getPaymentMethodId(),
                entity.getPaymentType(),
                entity.getPaidAt(),
                entity.getAmount(),
                entity.getStatus());
    }
}
