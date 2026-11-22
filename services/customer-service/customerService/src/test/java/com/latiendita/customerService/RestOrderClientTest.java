package com.latiendita.customerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.latiendita.customerService.customer.order.RestOrderClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RestOrderClientTest {

    private MockRestServiceServer server;
    private RestOrderClient client;

    @BeforeEach
    void setUp() {
        setIncomingHeaders(true);
        RestClient.Builder builder = RestClient.builder().baseUrl("http://order-service");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestOrderClient(builder.build());
    }

    @AfterEach
    void tearDown() {
        server.reset();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldForwardOnlyGatewayIdentityHeadersNeverBodyOrOtherHeaders() {
        server.expect(once(), path("/api/orders/admin/customers/stats"))
                .andExpect(header("X-User-Id", "admin-1"))
                .andExpect(header("X-User-Email", "admin@mail.com"))
                .andExpect(header("X-User-Roles", "admin"))
                .andExpect(headerDoesNotExist("X-Evil-Header"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.stats(List.of("c1", "c2"))).isEmpty();
        server.verify();
    }

    @Test
    void shouldDegradeToZerosWhenOrderServiceIsUnavailable() {
        server.expect(once(), path("/api/orders/admin/customers/stats"))
                .andRespond(withServerError());
        server.expect(once(), path("/api/orders/admin/customers/summary"))
                .andRespond(withServerError());

        assertThat(client.stats(List.of("c1"))).isEmpty();
        var summary = client.summary();
        assertThat(summary.recurringCustomers()).isZero();
        assertThat(summary.averageTicket()).isEqualByComparingTo("0.00");
        server.verify();
    }

    @Test
    void shouldNotForwardIdentityHeadersWhenTheGatewayDidNotInjectThem() {
        setIncomingHeaders(false);
        server.expect(once(), path("/api/orders/admin/customers/summary"))
                .andExpect(headerDoesNotExist("X-User-Id"))
                .andExpect(headerDoesNotExist("X-User-Roles"))
                .andRespond(withSuccess(
                        "{\"recurringCustomers\":0,\"averageTicket\":0.00}",
                        MediaType.APPLICATION_JSON));

        assertThat(client.summary().recurringCustomers()).isZero();
        server.verify();
    }

    private void setIncomingHeaders(boolean present) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (present) {
            request.addHeader("X-User-Id", "admin-1");
            request.addHeader("X-User-Email", "admin@mail.com");
            request.addHeader("X-User-Roles", "admin");
            request.addHeader("X-Evil-Header", "spoofed");
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private RequestMatcher path(String path) {
        return request -> request.getURI().getPath().equals(path);
    }
}
