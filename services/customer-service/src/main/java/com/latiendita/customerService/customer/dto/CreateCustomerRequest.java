package com.latiendita.customerService.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code authorizerSub} is kept for contract fidelity but the service always
 * overrides it with the trusted value from the X-User-Id gateway header.
 */
public record CreateCustomerRequest(
        String authorizerSub,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 30) String phone) {
}
