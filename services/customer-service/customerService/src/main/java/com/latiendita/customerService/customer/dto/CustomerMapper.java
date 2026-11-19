package com.latiendita.customerService.customer.dto;

import com.latiendita.customerService.customer.CustomerEntity;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerDto toDto(CustomerEntity entity) {
        return new CustomerDto(
                entity.getId(),
                entity.getAuthorizerSub(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getStatus(),
                entity.isNewsletterOptIn(),
                entity.getNewsletterConsentAt(),
                entity.getLastLoginAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static CustomerSummaryDto toSummaryDto(CustomerEntity entity) {
        return new CustomerSummaryDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getLastLoginAt());
    }

    public static NewsletterDto toNewsletterDto(CustomerEntity entity) {
        return new NewsletterDto(entity.isNewsletterOptIn(), entity.getNewsletterConsentAt());
    }
}
