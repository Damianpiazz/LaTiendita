package com.latiendita.cartService.product;

/**
 * Boundary to product-service. Keeping it as an interface lets the cart service
 * depend on server-side product facts without coupling to the HTTP transport.
 */
public interface ProductClient {

    ProductSnapshot findById(Long productId);
}
