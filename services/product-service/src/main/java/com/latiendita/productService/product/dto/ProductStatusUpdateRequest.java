package com.latiendita.productService.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Bulk publish/archive payload. */
public record ProductStatusUpdateRequest(
        @NotEmpty List<Long> productIds,
        @NotNull Boolean published
) {}
