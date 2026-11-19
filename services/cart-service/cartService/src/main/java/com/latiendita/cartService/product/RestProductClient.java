package com.latiendita.cartService.product;

import com.latiendita.cartService.exception.ProductNotFoundException;
import com.latiendita.cartService.exception.ProductServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestProductClient implements ProductClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Override
    public ProductSnapshot findById(Long productId) {
        Supplier<ProductDto> supplier = () -> restClient.get()
                .uri("/api/products/{productId}", productId)
                .retrieve()
                .body(ProductDto.class);
        try {
            ProductDto product = circuitBreaker.executeSupplier(() -> retry.executeSupplier(supplier));
            return new ProductSnapshot(
                    product.productId(),
                    product.title(),
                    product.imageUrl(),
                    product.price(),
                    product.quantity() == null ? 0 : product.quantity());
        } catch (CallNotPermittedException ex) {
            log.warn("Circuit open against product-service for product {}", productId);
            throw new ProductServiceUnavailableException(ex);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ProductNotFoundException(productId);
            }
            log.warn("product-service returned {} for product {}: {}",
                    ex.getStatusCode().value(), productId, ex.getMessage());
            throw new ProductServiceUnavailableException(ex);
        } catch (RestClientException ex) {
            log.warn("product-service unavailable: {}", ex.getMessage());
            throw new ProductServiceUnavailableException(ex);
        }
    }
}
