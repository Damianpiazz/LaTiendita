package com.latiendita.customerService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.customerService.seeder.model.SeedDataset;
import com.latiendita.customerService.seeder.model.SeededCustomer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class CustomerDataGeneratorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(25, 42, "SEED", true).generate())
                .isEqualTo(generator(25, 42, "SEED", true).generate());
    }

    @Test
    void shouldChangeNamesWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(25, 42, "SEED", true).generate();
        SeedDataset second = generator(25, 43, "SEED", true).generate();

        assertThat(second.customers().get(0).firstName())
                .isNotEqualTo(first.customers().get(0).firstName());
        assertThat(second.customers().get(0).authorizerSub())
                .isEqualTo(first.customers().get(0).authorizerSub());
        assertThat(second.customers().get(0).id())
                .isEqualTo(first.customers().get(0).id());
    }

    @Test
    void shouldRespectCountUniqueKeysAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, "SEED", true).generate();

        assertThat(dataset.customers()).hasSize(1000);
        assertThat(dataset.events()).hasSize(1000);
        assertThat(unique(dataset.customers().stream().map(SeededCustomer::id))).hasSize(1000);
        assertThat(unique(dataset.customers().stream().map(SeededCustomer::authorizerSub)))
                .hasSize(1000);
        assertThat(unique(dataset.customers().stream().map(SeededCustomer::email)))
                .hasSize(1000);
        assertThat(dataset.customers()).allSatisfy(customer -> {
            assertThat(customer.authorizerSub()).startsWith("SEED-SUB-");
            assertThat(customer.email()).startsWith("seed+").endsWith("@example.test");
            assertThat(customer.firstName()).hasSizeLessThanOrEqualTo(80).isNotBlank();
            assertThat(customer.lastName()).hasSizeLessThanOrEqualTo(80).isNotBlank();
            assertThat(customer.phone()).hasSizeLessThanOrEqualTo(30);
            assertThat(customer.status()).isIn("ACTIVE", "SUSPENDED");
            assertThat(customer.createdAt()).isNotNull();
            assertThat(customer.updatedAt()).isAfter(customer.createdAt());
            assertThat(customer.lastLoginAt()).isAfter(customer.createdAt());
            if (customer.newsletterOptIn()) {
                assertThat(customer.newsletterConsentAt()).isEqualTo(customer.createdAt());
            } else {
                assertThat(customer.newsletterConsentAt()).isNull();
            }
        });
    }

    @Test
    void shouldDeriveCrossServiceIdsFromPrefixAndBase() {
        SeedDataset dataset = generator(5, 42, "SEED", false).generate();
        UUID expected = UUID.nameUUIDFromBytes(
                "SEED-CUST-9000001".getBytes(StandardCharsets.UTF_8));

        assertThat(dataset.customers().get(0).id()).isEqualTo(expected);
        assertThat(dataset.events()).isEmpty();
        assertThat(generator(5, 42, "OTHER", false).generate().customers().get(0).id())
                .isNotEqualTo(expected);
    }

    @Test
    void shouldEmitAlreadySentEventsWithValidPayload() throws JacksonException {
        SeedDataset dataset = generator(3, 7, "TEST", true).generate();
        SeededCustomer customer = dataset.customers().get(0);
        var event = dataset.events().get(0);

        assertThat(event.topic()).isEqualTo("customer-events");
        assertThat(event.eventType()).isEqualTo("CUSTOMER_CREATED");
        assertThat(event.aggregateId()).isEqualTo(customer.id().toString());
        assertThat(event.sentAt()).isAfter(event.createdAt());

        Map<String, Object> payload = mapper.readValue(
                event.payload(), new TypeReference<>() {});
        assertThat(payload).containsEntry("type", "CUSTOMER_CREATED")
                .containsEntry("id", customer.id().toString())
                .containsEntry("sub", customer.authorizerSub())
                .containsEntry("email", customer.email())
                .containsEntry("source", "TEST");
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static CustomerDataGenerator generator(int count, long seed,
                                                    String prefix, boolean includeInfra) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix(prefix);
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        properties.setIncludeInfra(includeInfra);
        return new CustomerDataGenerator(properties);
    }
}
