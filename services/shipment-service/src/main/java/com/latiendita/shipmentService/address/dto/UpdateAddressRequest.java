package com.latiendita.shipmentService.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
        @NotBlank @Size(max = 120) String recipientName,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Size(max = 200) String addressLine,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String province,
        @NotBlank @Size(max = 10) String postalCode,
        @Size(max = 200) String references,
        boolean isDefault) {
}
