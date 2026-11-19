package com.latiendita.shipmentService.shipment.dto;

import com.latiendita.shipmentService.shipment.ShipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ShipmentResponse(
        Long id,
        Long orderId,
        ShipmentStatus status,
        BigDecimal shippingCost,
        String currency,
        String recipientName,
        String phone,
        String addressLine,
        String city,
        String province,
        String postalCode,
        String references,
        LocalDate estimatedFrom,
        LocalDate estimatedTo,
        Instant dispatchedAt,
        Instant deliveredAt) {
}
