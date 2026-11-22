package com.latiendita.customerService.customer.order;

import java.math.BigDecimal;
import java.time.Instant;

/** Order-owned aggregate received from the frozen order-service stats endpoint. */
public record CustomerOrderStats(
        String customerId,
        long orderCount,
        BigDecimal totalSpent,
        Instant lastPurchaseAt
) {
}
