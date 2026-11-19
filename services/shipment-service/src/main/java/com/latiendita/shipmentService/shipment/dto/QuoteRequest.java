package com.latiendita.shipmentService.shipment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuoteRequest(
        @NotNull @DecimalMin(value = "0", message = "subtotal debe ser mayor o igual a 0")
        BigDecimal subtotal) {
}
