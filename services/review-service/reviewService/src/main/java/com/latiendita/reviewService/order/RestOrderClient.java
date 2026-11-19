package com.latiendita.reviewService.order;

import com.latiendita.reviewService.CurrentUser;
import com.latiendita.reviewService.exception.OrderServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class RestOrderClient implements OrderClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public RestOrderClient(@Qualifier("orderRestClient") RestClient restClient,
                           @Qualifier("orderCircuitBreaker") CircuitBreaker circuitBreaker,
                           @Qualifier("orderRetry") Retry retry) {
        this.restClient = restClient;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    @Override
    public List<OrderDto> findPurchases() {
        String userId = CurrentUser.id();
        Supplier<OrderDto.Page> supplier = () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/orders").queryParam("size", 1000).build())
                .header(CurrentUser.ID_HEADER, userId)
                .retrieve()
                .body(OrderDto.Page.class);
        try {
            OrderDto.Page page = circuitBreaker.executeSupplier(() -> retry.executeSupplier(supplier));
            return page == null || page.content() == null ? List.of() : page.content();
        } catch (CallNotPermittedException ex) {
            log.warn("Circuit open against order-service");
            throw new OrderServiceUnavailableException(ex);
        } catch (RestClientResponseException ex) {
            log.warn("order-service returned {}: {}", ex.getStatusCode().value(), ex.getMessage());
            throw new OrderServiceUnavailableException(ex);
        } catch (RestClientException ex) {
            log.warn("order-service unavailable: {}", ex.getMessage());
            throw new OrderServiceUnavailableException(ex);
        }
    }
}
