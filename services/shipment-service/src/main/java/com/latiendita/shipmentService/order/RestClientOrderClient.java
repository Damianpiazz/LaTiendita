package com.latiendita.shipmentService.order;

import com.latiendita.shipmentService.exception.OrderNotFoundException;
import com.latiendita.shipmentService.exception.OrderServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestClientOrderClient implements OrderClient {

    private final RestClient restClient;

    public RestClientOrderClient(RestClient.Builder builder,
                                 @Value("${order-service.base-url:http://localhost:8080}")
                                 String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public OrderDto findById(Long orderId) {
        try {
            OrderDto order = restClient.get()
                    .uri("/api/orders/{orderId}", orderId)
                    .retrieve()
                    .body(OrderDto.class);
            if (order == null) {
                throw new OrderServiceUnavailableException(
                        new IllegalStateException("Respuesta vacía al buscar el pedido " + orderId));
            }
            return order;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new OrderNotFoundException(orderId);
        } catch (RestClientException ex) {
            throw new OrderServiceUnavailableException(ex);
        }
    }
}
