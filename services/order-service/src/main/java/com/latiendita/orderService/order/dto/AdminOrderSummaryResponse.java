package com.latiendita.orderService.order.dto;

import java.math.BigDecimal;
import java.util.List;

/** Order-owned KPIs of the admin dashboard; amounts exclude cancelled orders. */
public record AdminOrderSummaryResponse(
        BigDecimal salesToday,
        BigDecimal salesYesterday,
        long ordersToday,
        long pendingOrders,
        long pendingWithoutPayment,
        long weekOrders,
        BigDecimal previousWeekSales,
        List<AdminSalesDayResponse> salesByDay,
        List<AdminOrderResponse> recentOrders
) {}
