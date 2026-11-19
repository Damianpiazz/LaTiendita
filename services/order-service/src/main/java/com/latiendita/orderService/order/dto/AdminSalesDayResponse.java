package com.latiendita.orderService.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Aggregated sales for one calendar day (UTC) of the seven-day admin series. */
public record AdminSalesDayResponse(
        LocalDate date,
        BigDecimal total,
        long orders,
        BigDecimal averageTicket
) {}
