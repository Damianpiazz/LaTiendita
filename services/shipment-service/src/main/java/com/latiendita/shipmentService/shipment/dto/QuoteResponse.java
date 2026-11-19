package com.latiendita.shipmentService.shipment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteResponse(
        BigDecimal shippingCost,
        String currency,
        LocalDate estimatedFrom,
        LocalDate estimatedTo) {
}
