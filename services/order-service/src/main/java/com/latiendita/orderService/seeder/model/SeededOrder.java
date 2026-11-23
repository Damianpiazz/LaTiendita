package com.latiendita.orderService.seeder.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Pedido sintético, con las columnas reales de `orders`. */
public record SeededOrder(
        Long id,
        String orderNumber,
        String customerId,
        String status,
        BigDecimal total,
        String cancelReason,
        boolean refundRequired,
        Instant createdAt,
        Instant updatedAt,
        long version
) {}
