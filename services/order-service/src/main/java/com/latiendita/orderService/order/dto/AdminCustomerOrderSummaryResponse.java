package com.latiendita.orderService.order.dto;

import java.math.BigDecimal;

/** Order-owned KPIs composed by customer-service for the Admin Clientes header. */
public record AdminCustomerOrderSummaryResponse(
        long recurringCustomers,
        BigDecimal averageTicket
) {
}
