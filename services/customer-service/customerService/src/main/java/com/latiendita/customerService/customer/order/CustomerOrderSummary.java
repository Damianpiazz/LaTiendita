package com.latiendita.customerService.customer.order;

import java.math.BigDecimal;

/** Order-owned KPIs received from the frozen order-service summary endpoint. */
public record CustomerOrderSummary(
        long recurringCustomers,
        BigDecimal averageTicket
) {
}
