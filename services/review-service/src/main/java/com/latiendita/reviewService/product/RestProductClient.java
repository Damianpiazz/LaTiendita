package com.latiendita.reviewService.product;

import com.latiendita.reviewService.exception.ProductNotFoundException;
import com.latiendita.reviewService.exception.ProductServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class RestProductClient implements ProductClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public RestProductClient(@Qualifier("productRestClient") RestClient restClient,
                             @Qualifier("productCircuitBreaker") CircuitBreaker circuitBreaker,
                             @Qualifier("productRetry") Retry retry) {
        this.restClient = restClient;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    @Override
    public void requireExisting(Long productId) {
        Supplier<Void> supplier = () -> {
            restClient.get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        };
        try {
            circuitBreaker.executeSupplier(() -> retry.executeSupplier(supplier));
        } catch (CallNotPermittedException ex) {
            log.warn("Circuit open against product-service for product {}", productId);
            throw new ProductServiceUnavailableException(ex);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ProductNotFoundException(productId);
            }
            log.warn("product-service returned {} for product {}",
                    ex.getStatusCode().value(), productId);
            throw new ProductServiceUnavailableException(ex);
        } catch (RestClientException ex) {
            log.warn("product-service unavailable: {}", ex.getMessage());
            throw new ProductServiceUnavailableException(ex);
        }
    }
}
