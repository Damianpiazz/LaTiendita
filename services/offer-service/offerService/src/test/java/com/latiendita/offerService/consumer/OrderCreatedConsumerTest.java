package com.latiendita.offerService.consumer;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.latiendita.offerService.coupon.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderCreatedConsumerTest {

    private static final String ORDER_CREATED = """
            {"type":"ORDER_CREATED","orderId":7,"orderNumber":"7",\
            "customerId":"customer-1","total":2049.48,"couponCode":"SAVE10",\
            "occurredAt":"2026-11-19T12:00:00Z"}
            """;

    @Mock
    private CouponService couponService;

    private OrderCreatedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCreatedConsumer(couponService, new ObjectMapper());
    }

    @Test
    void shouldRecordCouponUsageFromOrderCreatedEvent() {
        consumer.onOrderEvent(ORDER_CREATED);

        verify(couponService).recordUsageForCode("SAVE10", "customer-1", 7L);
    }

    @Test
    void shouldIgnoreUnrelatedEventTypes() {
        consumer.onOrderEvent("""
                {"type":"ORDER_CANCELLED","orderId":7,"orderNumber":"7"}
                """);

        verify(couponService, never()).recordUsageForCode(anyString(), anyString(), anyLong());
    }

    @Test
    void shouldDiscardMalformedJsonWithoutFailing() {
        consumer.onOrderEvent("esto no es json {{{");
        consumer.onOrderEvent("");

        verify(couponService, never()).recordUsageForCode(anyString(), anyString(), anyLong());
    }

    @Test
    void shouldIgnoreOrderCreatedWithoutCouponCode() {
        consumer.onOrderEvent("""
                {"type":"ORDER_CREATED","orderId":7,"orderNumber":"7",\
                "customerId":"customer-1","total":2049.48,\
                "occurredAt":"2026-11-19T12:00:00Z"}
                """);

        verify(couponService, never()).recordUsageForCode(anyString(), anyString(), anyLong());
    }
}
