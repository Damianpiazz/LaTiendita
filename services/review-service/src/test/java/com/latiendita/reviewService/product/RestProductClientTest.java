package com.latiendita.reviewService.product;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.latiendita.reviewService.exception.ProductNotFoundException;
import com.latiendita.reviewService.exception.ProductServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

class RestProductClientTest {

    private static final String URL = "http://product-service/api/products/42";

    private MockRestServiceServer server;
    private RestProductClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://product-service");
        server = MockRestServiceServer.bindTo(builder).build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("productService", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(ResourceAccessException.class, HttpServerErrorException.class)
                .build());
        Retry retry = Retry.of("productService", RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(attempt -> 1L)
                .retryExceptions(ResourceAccessException.class, HttpServerErrorException.class)
                .build());
        client = new RestProductClient(builder.build(), circuitBreaker, retry);
    }

    @AfterEach
    void tearDown() {
        server.reset();
    }

    @Test
    void shouldAcceptExistingProduct() {
        server.expect(once(), requestTo(URL)).andRespond(withSuccess(
                "{\"productId\":42,\"title\":\"Notebook\",\"price\":99.99}", MediaType.APPLICATION_JSON));

        assertThatCode(() -> client.requireExisting(42L)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void shouldThrowNotFoundWithoutRetryWhenProductDoesNotExist() {
        server.expect(once(), requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.requireExisting(42L))
                .isInstanceOf(ProductNotFoundException.class);
        server.verify();
    }

    @Test
    void shouldThrowUnavailableWhenProductServiceFails() {
        server.expect(times(3), requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> client.requireExisting(42L))
                .isInstanceOf(ProductServiceUnavailableException.class);
        server.verify();
    }
}
