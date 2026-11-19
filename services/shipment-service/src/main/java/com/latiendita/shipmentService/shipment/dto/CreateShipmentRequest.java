package com.latiendita.shipmentService.shipment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateShipmentRequest(
        @NotNull Long orderId,
        Long addressId,
        @Valid AddressInput address,
        @NotNull @DecimalMin(value = "0", message = "shippingCost debe ser mayor o igual a 0")
        BigDecimal shippingCost,
        @NotBlank @Size(max = 3) String currency,
        @NotNull LocalDate estimatedFrom,
        @NotNull LocalDate estimatedTo) {

    public record AddressInput(
            @NotBlank @Size(max = 120) String recipientName,
            @NotBlank @Size(max = 30) String phone,
            @NotBlank @Size(max = 200) String addressLine,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String province,
            @NotBlank @Size(max = 10) String postalCode,
            @Size(max = 200) String references) {
    }
}
