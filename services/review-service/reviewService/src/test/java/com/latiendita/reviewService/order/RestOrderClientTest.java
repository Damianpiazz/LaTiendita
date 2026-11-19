package com.latiendita.reviewService.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.latiendita.reviewService.exception.OrderServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RestOrderClientTest {

    private static final String URL = "http://order-service/api/orders?size=1000";
    private static final String PAGE_JSON = """
            {"content":[{"id":9,"orderNumber":"ORD-9","customerId":"auth0|customer-1",
             "status":"PAID","total":99.99,"items":[{"productId":42,"quantity":1,
             "unitPrice":99.99,"subtotal":99.99}]}],"totalElements":1}
            """;

    private MockRestServiceServer server;
    private CircuitBreaker circuitBreaker;
    private RestOrderClient client;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "auth0|customer-1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RestClient.Builder builder = RestClient.builder().baseUrl("http://order-service");
        server = MockRestServiceServer.bindTo(builder).build();
        circuitBreaker = CircuitBreaker.of("orderService", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(ResourceAccessException.class, HttpServerErrorException.class)
                .build());
        Retry retry = Retry.of("orderService", RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(attempt -> 1L)
                .retryExceptions(ResourceAccessException.class, HttpServerErrorException.class)
                .build());
        client = new RestOrderClient(builder.build(), circuitBreaker, retry);
    }

    @AfterEach
    void tearDown() {
        server.reset();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldReturnPurchasesForwardingTheUserHeader() {
        server.expect(once(), requestTo(URL))
                .andExpect(header("X-User-Id", "auth0|customer-1"))
                .andRespond(withSuccess(PAGE_JSON, MediaType.APPLICATION_JSON));

        List<OrderDto> orders = client.findPurchases();

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).status()).isEqualTo("PAID");
        assertThat(orders.get(0).items().get(0).productId()).isEqualTo(42L);
        server.verify();
    }

    @Test
    void shouldRetryOn5xxAndThrowUnavailableAfterExhaustingAttempts() {
        server.expect(times(3), requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> client.findPurchases())
                .isInstanceOf(OrderServiceUnavailableException.class);

        server.verify();
    }

    @Test
    void shouldThrowUnavailableWhenCircuitIsOpen() {
        circuitBreaker.transitionToOpenState();

        assertThatThrownBy(() -> client.findPurchases())
                .isInstanceOf(OrderServiceUnavailableException.class)
                .hasRootCauseInstanceOf(
                        io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);
        server.verify();
    }
}
