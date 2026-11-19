package com.latiendita.shipmentService.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.latiendita.shipmentService.consumer.event.OrderCancelledEvent;
import com.latiendita.shipmentService.shipment.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderCancelledConsumerTest {

    /** order-service publishes ORDER_CANCELLED without a `type` field (doc §6.3). */
    private static final String CANCELLED = """
            {"orderId":7,"orderNumber":"ord-7","customerId":"user-1",\
            "reason":"changed mind","refundRequired":true,\
            "occurredAt":"2026-10-12T12:00:00Z"}
            """;

    @Mock
    private ShipmentService shipmentService;

    private OrderCancelledConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCancelledConsumer(shipmentService, new ObjectMapper());
    }

    @Test
    void shouldDelegateCancellationPublishedWithoutTypeField() {
        consumer.onOrderEvent(CANCELLED);

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(shipmentService).cancelForOrder(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(7L);
        assertThat(captor.getValue().reason()).isEqualTo("changed mind");
        assertThat(captor.getValue().refundRequired()).isTrue();
    }

    @Test
    void shouldDelegateCancellationWhenExplicitTypeIsPresent() {
        consumer.onOrderEvent("""
                {"type":"ORDER_CANCELLED","orderId":7,"customerId":"user-1",\
                "reason":"fraud","refundRequired":false}
                """);

        verify(shipmentService).cancelForOrder(any(OrderCancelledEvent.class));
    }

    @Test
    void shouldIgnoreUnrelatedEventTypes() {
        consumer.onOrderEvent("""
                {"type":"ORDER_CREATED","orderId":7,"customerId":"user-1"}
                """);

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldDiscardMalformedJsonWithoutFailing() {
        consumer.onOrderEvent("esto no es json {{{");
        consumer.onOrderEvent("");

        verifyNoInteractions(shipmentService);
    }
}
