package com.latiendita.productService.product.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Bulk delete payload. */
public record ProductIdsRequest(@NotEmpty List<Long> productIds) {}
