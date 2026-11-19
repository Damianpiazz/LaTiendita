package com.latiendita.orderService.order.dto;

import java.math.BigDecimal;
import java.util.List;

/** Order-owned KPIs of the admin dashboard; amounts exclude cancelled orders. */
public record AdminOrderSummaryResponse(
        BigDecimal salesToday,
        long ordersToday,
        long pendingOrders,
        long pendingWithoutPayment,
        List<AdminSalesDayResponse> salesByDay,
        List<AdminOrderResponse> recentOrders
) {}
