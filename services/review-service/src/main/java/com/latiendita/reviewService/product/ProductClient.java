package com.latiendita.reviewService.product;

/** Boundary to product-service for validating that a reviewed product exists. */
public interface ProductClient {

    void requireExisting(Long productId);
}
