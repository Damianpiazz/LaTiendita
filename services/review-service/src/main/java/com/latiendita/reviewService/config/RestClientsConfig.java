package com.latiendita.reviewService.config;

import com.latiendita.reviewService.correlation.CorrelationIdInterceptor;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientsConfig {

    @Bean
    RestClient orderRestClient(ObservationRegistry observationRegistry,
                               CorrelationIdInterceptor correlationIdInterceptor,
                               @Value("${order-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .requestInterceptor(correlationIdInterceptor)
                .build();
    }

    @Bean
    RestClient productRestClient(ObservationRegistry observationRegistry,
                                 CorrelationIdInterceptor correlationIdInterceptor,
                                 @Value("${product-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .requestInterceptor(correlationIdInterceptor)
                .build();
    }
}
