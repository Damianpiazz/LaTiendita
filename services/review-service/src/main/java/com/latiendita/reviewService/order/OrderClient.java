package com.latiendita.reviewService.order;

import java.util.List;

/** Boundary to order-service for purchase verification. */
public interface OrderClient {

    List<OrderDto> findPurchases();
}
