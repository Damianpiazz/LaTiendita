package com.latiendita.shipmentService.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Guards the wire contract consumed by order-service's ShipmentDispatchedConsumer.
 * The field names and types below must stay identical to
 * {@code com.latiendita.orderService.order.dto.event.ShipmentDispatchedEvent}.
 */
class ShipmentDispatchedPayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeExactFieldsAndTypesExpectedByOrderServiceConsumer() {
        Instant dispatchedAt = Instant.parse("2026-10-12T12:00:00Z");
        Instant occurredAt = Instant.parse("2026-10-12T12:01:00Z");
        ShipmentDispatchedEvent event = new ShipmentDispatchedEvent(
                "SHIPMENT_DISPATCHED", 11L, 7L, "user-1",
                dispatchedAt, LocalDate.parse("2026-10-19"), occurredAt);

        String json = objectMapper.writeValueAsString(event);
        Map<String, Object> node = objectMapper.readValue(json, Map.class);

        assertThat(node).hasSize(7);
        assertThat(node.keySet()).containsExactlyInAnyOrder(
                "type", "shipmentId", "orderId", "customerId",
                "dispatchedAt", "estimatedTo", "occurredAt");
        assertThat(node.get("type")).isEqualTo("SHIPMENT_DISPATCHED");
        assertThat(node.get("shipmentId")).isInstanceOf(Number.class);
        assertThat(((Number) node.get("shipmentId")).longValue()).isEqualTo(11L);
        assertThat(node.get("orderId")).isInstanceOf(Number.class);
        assertThat(((Number) node.get("orderId")).longValue()).isEqualTo(7L);
        assertThat(node.get("customerId")).isEqualTo("user-1");
        assertThat(node.get("dispatchedAt")).isEqualTo("2026-10-12T12:00:00Z");
        assertThat(node.get("estimatedTo")).isEqualTo("2026-10-19");
        assertThat(node.get("occurredAt")).isEqualTo("2026-10-12T12:01:00Z");
    }

    @Test
    void shouldDeserializeIntoOrderServiceRecordShape() {
        String json = """
                {"type":"SHIPMENT_DISPATCHED","shipmentId":11,"orderId":7,"customerId":"1",\
                "dispatchedAt":"2026-10-12T12:00:00Z","estimatedTo":"2026-10-16",\
                "occurredAt":"2026-10-12T12:00:00Z"}
                """;

        OrderServiceContract decoded = objectMapper.readValue(json, OrderServiceContract.class);

        assertThat(decoded.type()).isEqualTo("SHIPMENT_DISPATCHED");
        assertThat(decoded.shipmentId()).isEqualTo(11L);
        assertThat(decoded.orderId()).isEqualTo(7L);
        assertThat(decoded.customerId()).isEqualTo("1");
        assertThat(decoded.dispatchedAt()).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        assertThat(decoded.estimatedTo()).isEqualTo(LocalDate.parse("2026-10-16"));
        assertThat(decoded.occurredAt()).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
    }

    /** Exact mirror of the consumer DTO owned by order-service. */
    record OrderServiceContract(
            String type,
            Long shipmentId,
            Long orderId,
            String customerId,
            Instant dispatchedAt,
            LocalDate estimatedTo,
            Instant occurredAt) {
    }
}
