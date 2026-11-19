package com.latiendita.shipmentService.address.dto;

public record AddressResponse(
        Long id,
        String recipientName,
        String phone,
        String addressLine,
        String city,
        String province,
        String postalCode,
        String references,
        boolean isDefault) {
}
