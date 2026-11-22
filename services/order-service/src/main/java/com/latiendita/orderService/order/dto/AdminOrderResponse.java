package com.latiendita.orderService.order.dto;

import com.latiendita.orderService.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** One row of the global admin orders table. */
public record AdminOrderResponse(
        Long id,
        String orderNumber,
        String customerId,
        String customerEmail,
        String paymentMethod,
        OrderStatus status,
        BigDecimal total,
        int itemCount,
        Instant createdAt
) {}
