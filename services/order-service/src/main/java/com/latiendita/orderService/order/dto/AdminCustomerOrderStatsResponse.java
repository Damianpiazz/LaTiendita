package com.latiendita.orderService.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Order-owned aggregate for one customer in the Admin Clientes table. */
public record AdminCustomerOrderStatsResponse(
        String customerId,
        long orderCount,
        BigDecimal totalSpent,
        Instant lastPurchaseAt
) {
}
