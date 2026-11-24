package com.latiendita.customerService.customer.order;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** HTTP adapter that forwards only gateway-injected identity headers to order-service. */
@Component
@Slf4j
public class RestOrderClient implements OrderClient {

    private static final String ID_HEADER = "X-User-Id";
    private static final String EMAIL_HEADER = "X-User-Email";
    private static final String ROLES_HEADER = "X-User-Roles";

    private final RestClient restClient;

    public RestOrderClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<CustomerOrderStats> stats(List<String> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }
        String joinedIds = String.join(",", customerIds);
        try {
            RequestHeadersSpec<?> request = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/orders/admin/customers/stats")
                            .queryParam("customerIds", joinedIds)
                            .build());
            forwardIdentityHeaders(request);
            List<CustomerOrderStats> body = request.retrieve()
                    .body(new ParameterizedTypeReference<List<CustomerOrderStats>>() {});
            return body == null ? List.of() : body;
        } catch (RestClientException ex) {
            log.warn("order-service stats unavailable: {}", ex.getMessage());
            return List.of();
        }
    }

    @Override
    public CustomerOrderSummary summary() {
        try {
            RequestHeadersSpec<?> request = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/orders/admin/customers/summary").build());
            forwardIdentityHeaders(request);
            CustomerOrderSummary body = request.retrieve().body(CustomerOrderSummary.class);
            return body == null
                    ? new CustomerOrderSummary(0L, BigDecimal.ZERO.setScale(2))
                    : body;
        } catch (RestClientException ex) {
            log.warn("order-service summary unavailable: {}", ex.getMessage());
            return new CustomerOrderSummary(0L, BigDecimal.ZERO.setScale(2));
        }
    }

    private static void forwardIdentityHeaders(RequestHeadersSpec<?> request) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return;
        }
        copyHeader(request, servletAttributes, ID_HEADER);
        copyHeader(request, servletAttributes, EMAIL_HEADER);
        copyHeader(request, servletAttributes, ROLES_HEADER);
    }

    private static void copyHeader(RequestHeadersSpec<?> request,
                                   ServletRequestAttributes attributes, String name) {
        String value = attributes.getRequest().getHeader(name);
        if (value != null && !value.isBlank()) {
            request.header(name, value);
        }
    }
}
