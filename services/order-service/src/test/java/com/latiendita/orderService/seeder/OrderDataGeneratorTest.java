package com.latiendita.orderService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.orderService.seeder.model.SeedDataset;
import com.latiendita.orderService.seeder.model.SeededOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class OrderDataGeneratorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(25, 42, "SEED", true).generate())
                .isEqualTo(generator(25, 42, "SEED", true).generate());
    }

    @Test
    void shouldChangePricesWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(25, 42, "SEED", true).generate();
        SeedDataset second = generator(25, 43, "SEED", true).generate();

        assertThat(second.orders().get(0).total())
                .isNotEqualTo(first.orders().get(0).total());
        assertThat(second.orders().get(0).id()).isEqualTo(first.orders().get(0).id());
        assertThat(second.orders().get(0).orderNumber())
                .isEqualTo(first.orders().get(0).orderNumber());
    }

    @Test
    void shouldRespectCountUniqueKeysAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, "SEED", true).generate();

        assertThat(dataset.orders()).hasSize(1000);
        assertThat(dataset.items()).hasSizeGreaterThanOrEqualTo(1000);
        assertThat(unique(dataset.orders().stream().map(SeededOrder::id))).hasSize(1000);
        assertThat(unique(dataset.orders().stream().map(SeededOrder::orderNumber))).hasSize(1000);
        assertThat(unique(dataset.items().stream().map(item -> item.orderId() + ":"
                + item.productId()))).hasSize(dataset.items().size());
        assertThat(dataset.orders()).allSatisfy(order -> {
            assertThat(order.orderNumber()).startsWith("SEED-ORD-").hasSizeLessThanOrEqualTo(36);
            assertThat(order.customerId()).isNotBlank();
            assertThat(order.status()).isIn("PENDING", "PAID", "SHIPPED", "DELIVERED",
                    "CANCELLED");
            assertThat(order.total()).isPositive();
            assertThat(order.version()).isZero();
            assertThat(order.updatedAt()).isAfter(order.createdAt());
            if ("CANCELLED".equals(order.status())) {
                assertThat(order.cancelReason()).isNotBlank();
            } else {
                assertThat(order.cancelReason()).isNull();
                assertThat(order.refundRequired()).isFalse();
            }
        });
    }

    @Test
    void shouldDeriveCrossServiceIdsFromPrefixAndBase() {
        SeedDataset dataset = generator(5, 42, "SEED", false).generate();
        UUID expected = UUID.nameUUIDFromBytes(
                "SEED-CUST-9000001".getBytes(StandardCharsets.UTF_8));

        assertThat(dataset.orders().get(0).customerId()).isEqualTo(expected.toString());
        assertThat(dataset.items().get(0).productId()).isEqualTo(9_100_000L);
        assertThat(dataset.items().get(0).sku()).isEqualTo("SEED-SKU-100000");
        assertThat(dataset.events()).isEmpty();
        assertThat(generator(5, 42, "OTHER", false).generate().orders().get(0).customerId())
                .isNotEqualTo(expected);
    }

    @Test
    void shouldEmitAlreadySentEventsWithValidPayload() throws JacksonException {
        SeedDataset dataset = generator(4, 7, "TEST", true).generate();
        SeededOrder order = dataset.orders().get(0);
        var event = dataset.events().get(0);

        assertThat(event.topic()).isEqualTo("order-events");
        assertThat(event.eventType()).isEqualTo("ORDER_CREATED");
        assertThat(event.aggregateId()).isEqualTo(String.valueOf(order.id()));
        assertThat(event.sentAt()).isAfter(event.createdAt());

        Map<String, Object> payload = mapper.readValue(event.payload(),
                new TypeReference<>() {});
        assertThat(((Number) payload.get("orderId")).longValue()).isEqualTo(order.id());
        assertThat(payload).containsEntry("orderNumber", order.orderNumber())
                .containsEntry("customerId", order.customerId().toString())
                .containsEntry("source", "TEST");
        assertThat(((Number) payload.get("total")).doubleValue())
                .isEqualTo(order.total().doubleValue());
        assertThat(dataset.events()).allSatisfy(e ->
                assertThat(e.payload()).contains("\"source\":\"TEST\""));
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static OrderDataGenerator generator(int count, long seed,
                                                 String prefix, boolean includeInfra) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix(prefix);
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        properties.setIncludeInfra(includeInfra);
        return new OrderDataGenerator(properties);
    }
}
