package com.latiendita.reviewService.order;

import java.util.List;

public record OrderDto(
        Long id,
        String status,
        List<Item> items
) {
    public record Item(Long productId) {
    }

    /** Minimal view of Spring Data's Page serialization returned by order-service. */
    public record Page(List<OrderDto> content) {
    }
}
