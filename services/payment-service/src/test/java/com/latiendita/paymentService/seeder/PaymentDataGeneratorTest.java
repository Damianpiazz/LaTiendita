package com.latiendita.paymentService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.paymentService.seeder.model.SeedDataset;
import com.latiendita.paymentService.seeder.model.SeededPayment;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class PaymentDataGeneratorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(25, 42, "SEED", true).generate())
                .isEqualTo(generator(25, 42, "SEED", true).generate());
    }

    @Test
    void shouldChangeAmountsWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(25, 42, "SEED", true).generate();
        SeedDataset second = generator(25, 43, "SEED", true).generate();

        assertThat(second.payments().get(0).amount())
                .isNotEqualTo(first.payments().get(0).amount());
        assertThat(second.payments().get(0).id()).isEqualTo(first.payments().get(0).id());
        assertThat(second.payments().get(0).idempotencyKey())
                .isEqualTo(first.payments().get(0).idempotencyKey());
    }

    @Test
    void shouldRespectCountUniqueKeysAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, "SEED", true).generate();

        assertThat(dataset.payments()).hasSize(1000);
        assertThat(dataset.events()).hasSize(1000);
        assertThat(unique(dataset.payments().stream().map(SeededPayment::id))).hasSize(1000);
        assertThat(unique(dataset.payments().stream().map(SeededPayment::idempotencyKey)))
                .hasSize(1000);
        assertThat(unique(dataset.payments().stream().map(SeededPayment::providerPaymentId)))
                .hasSize(1000);
        assertThat(dataset.payments()).allSatisfy(payment -> {
            assertThat(payment.provider()).isEqualTo("MERCADO_PAGO");
            assertThat(payment.currency()).isEqualTo("ARS");
            assertThat(payment.amount()).isPositive();
            assertThat(payment.status()).isIn("PENDING", "REJECTED", "CANCELLED", "EXPIRED",
                    "APPROVED");
            assertThat(payment.externalReference()).startsWith("SEED-ORD-");
            assertThat(payment.updatedAt()).isAfter(payment.createdAt());
            assertThat(payment.version()).isZero();
            if ("APPROVED".equals(payment.status())) {
                assertThat(payment.paidAt()).isEqualTo(payment.updatedAt());
                assertThat(payment.expiresAt()).isNull();
            } else {
                assertThat(payment.paidAt()).isNull();
                assertThat(payment.statusDetail()).isNotBlank();
            }
        });
        assertThat(dataset.events()).allSatisfy(event -> {
            assertThat(event.status()).isEqualTo("PROCESSED");
            assertThat(event.attempts()).isEqualTo(1);
            assertThat(event.signatureValid()).isTrue();
            assertThat(event.processedAt()).isAfter(event.createdAt());
        });
    }

    @Test
    void shouldDeriveCrossServiceIdsFromPrefixAndBase() {
        SeedDataset dataset = generator(5, 42, "SEED", false).generate();
        UUID expected = UUID.nameUUIDFromBytes(
                "SEED-CUST-9000001".getBytes(StandardCharsets.UTF_8));

        assertThat(dataset.payments().get(0).customerId()).isEqualTo(expected.toString());
        assertThat(dataset.payments().get(0).orderId()).isEqualTo(9_200_000L);
        assertThat(dataset.payments().get(0).externalReference())
                .isEqualTo("SEED-ORD-9200000");
        assertThat(dataset.outbox()).isEmpty();
        assertThat(generator(5, 42, "OTHER", false).generate().payments().get(0).customerId())
                .isNotEqualTo(expected.toString());
    }

    @Test
    void shouldEmitAlreadySentEventsWithValidPayload() throws JacksonException {
        SeedDataset dataset = generator(4, 7, "TEST", true).generate();
        SeededPayment payment = dataset.payments().get(0);
        var event = dataset.outbox().get(0);

        assertThat(event.topic()).isEqualTo("payment-events");
        assertThat(event.eventType()).isEqualTo("PAYMENT_APPROVED");
        assertThat(event.aggregateId()).isEqualTo(String.valueOf(payment.orderId()));
        assertThat(event.sentAt()).isAfter(event.createdAt());

        Map<String, Object> payload = mapper.readValue(event.payload(),
                new TypeReference<>() {});
        assertThat(payload).containsEntry("type", "PAYMENT_APPROVED")
                .containsEntry("orderNumber", payment.externalReference())
                .containsEntry("customerId", payment.customerId())
                .containsEntry("provider", "MERCADO_PAGO")
                .containsEntry("source", "TEST");
        assertThat(((Number) payload.get("paymentId")).longValue()).isEqualTo(payment.id());
        assertThat(((Number) payload.get("amount")).doubleValue())
                .isEqualTo(payment.amount().doubleValue());
        assertThat(dataset.outbox()).allSatisfy(e ->
                assertThat(e.payload()).contains("\"source\":\"TEST\""));
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static PaymentDataGenerator generator(int count, long seed,
                                                   String prefix, boolean includeInfra) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix(prefix);
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        properties.setIncludeInfra(includeInfra);
        return new PaymentDataGenerator(properties);
    }
}
