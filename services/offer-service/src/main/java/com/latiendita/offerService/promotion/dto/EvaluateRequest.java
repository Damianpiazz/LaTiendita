package com.latiendita.offerService.promotion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record EvaluateRequest(
        @Valid @NotNull List<Item> items,
        @NotNull BigDecimal subtotal
) {

    public record Item(
            @NotNull Long productId,
            Long categoryId,
            @NotNull Integer quantity,
            @NotNull BigDecimal unitPrice
    ) {
    }
}
