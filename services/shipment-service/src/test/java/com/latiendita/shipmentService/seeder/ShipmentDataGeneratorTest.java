package com.latiendita.shipmentService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.shipmentService.seeder.model.SeedDataset;
import com.latiendita.shipmentService.seeder.model.SeededShipment;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class ShipmentDataGeneratorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(25, 42, "SEED", true).generate())
                .isEqualTo(generator(25, 42, "SEED", true).generate());
    }

    @Test
    void shouldChangeAddressesWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(25, 42, "SEED", true).generate();
        SeedDataset second = generator(25, 43, "SEED", true).generate();

        assertThat(second.shipments().get(0).shippingCost())
                .isNotEqualTo(first.shipments().get(0).shippingCost());
        assertThat(second.shipments().get(0).orderId())
                .isEqualTo(first.shipments().get(0).orderId());
        assertThat(second.shipments().get(0).customerId())
                .isEqualTo(first.shipments().get(0).customerId());
    }

    @Test
    void shouldRespectCountUniqueKeysAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, "SEED", true).generate();

        assertThat(dataset.shipments()).hasSize(1000);
        assertThat(dataset.addresses()).hasSize(1000);
        assertThat(unique(dataset.shipments().stream().map(SeededShipment::id))).hasSize(1000);
        assertThat(unique(dataset.shipments().stream().map(SeededShipment::orderId)))
                .hasSize(1000);
        assertThat(unique(dataset.shipments().stream().map(SeededShipment::customerId)))
                .hasSize(1000);
        assertThat(dataset.shipments()).allSatisfy(shipment -> {
            assertThat(shipment.customerId()).startsWith("SEED-CUST-");
            assertThat(shipment.orderId()).isPositive();
            assertThat(shipment.status()).isIn("AWAITING_PAYMENT", "PENDING_DISPATCH",
                    "DISPATCHED", "DELIVERED", "CANCELLED");
            assertThat(shipment.shippingCost()).isPositive();
            assertThat(shipment.currency()).isEqualTo("ARS");
            assertThat(shipment.recipientName()).isNotBlank();
            assertThat(shipment.phone()).isNotBlank();
            assertThat(shipment.estimatedTo()).isAfter(shipment.estimatedFrom());
            assertThat(shipment.version()).isZero();
            assertThat(shipment.updatedAt()).isAfterOrEqualTo(shipment.createdAt());
            boolean dispatched = shipment.dispatchedAt() != null;
            boolean delivered = shipment.deliveredAt() != null;
            assertThat(dispatched).isEqualTo(shipment.status().equals("DISPATCHED")
                    || shipment.status().equals("DELIVERED"));
            assertThat(delivered).isEqualTo(shipment.status().equals("DELIVERED"));
        });
        assertThat(dataset.addresses()).allSatisfy(address -> {
            assertThat(address.customerId()).startsWith("SEED-CUST-");
            assertThat(address.isDefault()).isTrue();
            assertThat(address.recipientName()).isNotBlank();
        });
    }

    @Test
    void shouldDeriveCrossServiceIdsFromPrefixAndBase() {
        SeedDataset dataset = generator(5, 42, "SEED", false).generate();

        assertThat(dataset.shipments().get(0).customerId()).isEqualTo("SEED-CUST-9000001");
        assertThat(dataset.shipments().get(0).orderId()).isEqualTo(9_200_000L);
        assertThat(dataset.addresses().get(0).customerId()).isEqualTo("SEED-CUST-9000001");
        assertThat(dataset.events()).isEmpty();
        assertThat(generator(5, 42, "OTHER", false).generate().shipments().get(0).customerId())
                .isEqualTo("OTHER-CUST-9000001");
    }

    @Test
    void shouldEmitAlreadySentEventsWithValidPayload() throws Exception {
        SeedDataset dataset = generator(6, 7, "TEST", true).generate();
        var event = dataset.events().get(0);
        SeededShipment shipment = dataset.shipments().stream()
                .filter(row -> String.valueOf(row.orderId()).equals(event.aggregateId()))
                .findFirst().orElseThrow();

        assertThat(event.topic()).isEqualTo("shipment-events");
        assertThat(event.eventType()).isIn("SHIPMENT_DISPATCHED", "SHIPMENT_DELIVERED",
                "SHIPMENT_CANCELLED");
        assertThat(event.sentAt()).isAfter(event.createdAt());

        Map<String, Object> payload = mapper.readValue(event.payload(),
                new TypeReference<>() {});
        assertThat(((Number) payload.get("shipmentId")).longValue()).isEqualTo(shipment.id());
        assertThat(((Number) payload.get("orderId")).longValue()).isEqualTo(shipment.orderId());
        assertThat(payload).containsEntry("customerId", shipment.customerId())
                .containsEntry("type", event.eventType())
                .containsEntry("source", "TEST");
        assertThat(dataset.events()).allSatisfy(row ->
                assertThat(row.payload()).contains("\"source\":\"TEST\""));
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static ShipmentDataGenerator generator(int count, long seed,
                                                   String prefix, boolean includeInfra) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix(prefix);
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        properties.setIncludeInfra(includeInfra);
        return new ShipmentDataGenerator(properties);
    }
}
