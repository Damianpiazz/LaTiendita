package com.latiendita.cartService.product;

import com.latiendita.cartService.correlation.CorrelationIdInterceptor;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient restClient(ObservationRegistry observationRegistry,
                          CorrelationIdInterceptor correlationIdInterceptor,
                          @Value("${product-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .requestInterceptor(correlationIdInterceptor)
                .build();
    }
}
