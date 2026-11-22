package com.latiendita.customerService.customer.dto;

import com.latiendita.customerService.customer.CustomerStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** Row of the Admin Clientes table, composed from customer data and order aggregates. */
public record AdminCustomerResponse(
        String customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        CustomerStatus status,
        Instant createdAt,
        Instant lastLoginAt,
        long orderCount,
        BigDecimal totalSpent,
        Instant lastPurchaseAt,
        CustomerSegment segment
) {
}
