package com.latiendita.customerService.customer.dto;

import java.math.BigDecimal;

/** KPI strip of the Admin Clientes wireframe. */
public record AdminCustomerSummaryResponse(
        long totalCustomers,
        long newThisMonth,
        long recurring,
        BigDecimal averageTicket
) {
}
