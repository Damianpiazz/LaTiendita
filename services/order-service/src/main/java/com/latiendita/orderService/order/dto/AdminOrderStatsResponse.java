package com.latiendita.orderService.order.dto;

/** Screen-level totals and status counts for the admin orders wireframe. */
public record AdminOrderStatsResponse(
        long totalOrders,
        long pendingDispatch,
        long cancelledInPeriod,
        StatusCounts statusCounts
) {
    public record StatusCounts(
            long todos,
            long pendiente,
            long pagado,
            long enviado,
            long entregado,
            long cancelado
    ) {}
}