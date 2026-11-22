package com.latiendita.customerService;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.customerService.customer.order.RestOrderClient;
import java.util.List;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

class T3IndependentOrderClientDegradationProbeTest {
    @Test
    void deadOrderServiceYieldsEmptyStatsAndZeroSummary() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://127.0.0.1:9");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(org.springframework.test.web.client.ExpectedCount.once(),
                org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(
                        org.hamcrest.Matchers.containsString("/api/orders/admin/customers/stats")))
                .andRespond(MockRestResponseCreators.withException(new java.io.IOException("connection refused")));
        server.expect(org.springframework.test.web.client.ExpectedCount.once(),
                org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo(
                        org.hamcrest.Matchers.containsString("/api/orders/admin/customers/summary")))
                .andRespond(MockRestResponseCreators.withException(new java.io.IOException("connection refused")));
        RestOrderClient client = new RestOrderClient(builder.build());
        assertThat(client.stats(List.of("c1"))).isEmpty();
        var summary = client.summary();
        assertThat(summary.recurringCustomers()).isZero();
        assertThat(summary.averageTicket()).isEqualByComparingTo("0.00");
        server.verify();
    }
}
