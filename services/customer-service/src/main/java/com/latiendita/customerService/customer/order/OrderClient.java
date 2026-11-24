package com.latiendita.customerService.customer.order;

import java.util.List;

/** Boundary to the order-owned aggregates used by Admin Clientes. */
public interface OrderClient {

    List<CustomerOrderStats> stats(List<String> customerIds);

    CustomerOrderSummary summary();
}
