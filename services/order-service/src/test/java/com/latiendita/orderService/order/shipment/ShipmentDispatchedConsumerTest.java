package com.latiendita.orderService.order.shipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.orderService.order.OrderEntity;
import com.latiendita.orderService.order.OrderRepository;
import com.latiendita.orderService.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ShipmentDispatchedConsumerTest {

    private static final String DISPATCHED = """
            {"type":"SHIPMENT_DISPATCHED","shipmentId":11,"orderId":7,"customerId":"1",\
            "dispatchedAt":"2026-10-12T12:00:00Z","estimatedTo":"2026-10-16",\
            "occurredAt":"2026-10-12T12:00:00Z"}
            """;

    @Mock
    private OrderRepository orderRepository;

    private ShipmentDispatchedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ShipmentDispatchedConsumer(orderRepository, new ObjectMapper());
    }

    @Test
    void shouldMovePaidOrderToShipped() {
        OrderEntity order = order(OrderStatus.PAID);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        consumer.onShipmentEvent(DISPATCHED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldIgnoreEventWhenOrderIsNotPaid() {
        OrderEntity order = order(OrderStatus.PENDING);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        consumer.onShipmentEvent(DISPATCHED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void shouldIgnoreOtherEventTypes() {
        consumer.onShipmentEvent("""
                {"type":"SHIPMENT_DELIVERED","shipmentId":11,"orderId":7}
                """);
        consumer.onShipmentEvent("""
                {"type":"SHIPMENT_CANCELLED","shipmentId":11,"orderId":7}
                """);

        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreMalformedJsonWithoutFailing() {
        consumer.onShipmentEvent("esto no es json {{{");
        consumer.onShipmentEvent("");

        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    void shouldFailWhenOrderDoesNotExistSoKafkaRetries() {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.onShipmentEvent(DISPATCHED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pedido inexistente");
    }

    private OrderEntity order(OrderStatus status) {
        OrderEntity order = OrderEntity.builder()
                .orderNumber(UUID.randomUUID().toString())
                .customerId("1")
                .status(status)
                .total(new BigDecimal("2049.48"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        order.setId(7L);
        return order;
    }
}
