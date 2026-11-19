package com.latiendita.shipmentService.order;

public interface OrderClient {

    OrderDto findById(Long orderId);

    record OrderDto(Long id, String status) {
    }
}
