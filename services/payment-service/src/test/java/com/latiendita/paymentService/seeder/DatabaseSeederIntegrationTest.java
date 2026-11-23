package com.latiendita.paymentService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Ejecuta el seeder real contra PostgreSQL y comprueba volumen, integridad e idempotencia. */
@Import(SeederTestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "seeder.enabled=false",
        "seeder.count=30",
        "seeder.batch-size=10",
        "seeder.random-seed=99",
        "seeder.include-infra=true",
        "seeder.external-id-prefix=SEED",
        "seeder.external-id-base=9000000"
})
@ActiveProfiles("test")
class DatabaseSeederIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseSeederService service;

    @Autowired
    private SeederProperties properties;

    @Autowired
    private SeederValidator validator;

    @Autowired
    private PaymentDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealRows();
        int expectedOutbox = generator.generate().outbox().size();

        SeedExecution first = service.seed();
        assertThat(first.executed()).isTrue();
        assertThat(first.payments()).isEqualTo(30);
        assertThat(first.events()).isEqualTo(30);
        assertThat(first.outbox()).isEqualTo(expectedOutbox);
        assertThat(count("SELECT count(*) FROM payments WHERE idempotency_key LIKE ?",
                "SEED-IDEM-%")).isEqualTo(30);
        assertThat(count("SELECT count(DISTINCT idempotency_key) FROM payments "
                + "WHERE idempotency_key LIKE ?", "SEED-IDEM-%")).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM payment_events WHERE notification_id LIKE ?",
                "SEED-NOTIF-%")).isEqualTo(30);
        assertThat(count("""
                SELECT count(*) FROM outbox_events e
                JOIN payments p ON CAST(p.order_id AS varchar(64)) = e.aggregate_id
                WHERE p.idempotency_key LIKE ? AND e.status = 'SENT' AND e.sent_at IS NOT NULL
                """, "SEED-IDEM-%")).isEqualTo(expectedOutbox);
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'payment' "
                + "AND version = 'payment-v1'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM payments WHERE idempotency_key = 'real-key'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM payment_events "
                + "WHERE notification_id = 'real-notification'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM payments")).isEqualTo(31);

        SeedExecution second = service.seed();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM payments WHERE idempotency_key LIKE ?",
                "SEED-IDEM-%")).isEqualTo(30);

        jdbc.update("DELETE FROM payment_events WHERE notification_id = 'SEED-NOTIF-9400000'");
        assertThatThrownBy(() -> validator.validate(generator.generate(),
                DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("notificaciones sintéticas");

        jdbc.update("DELETE FROM seeder_markers WHERE service = 'payment'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.seed();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM payments WHERE idempotency_key LIKE ?",
                "SEED-IDEM-%")).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM payment_events WHERE notification_id LIKE ?",
                "SEED-NOTIF-%")).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?",
                "%\"source\":\"SEED\"%")).isEqualTo(expectedOutbox);
        assertThat(count("SELECT count(*) FROM payments WHERE idempotency_key = 'real-key'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM payment_events "
                + "WHERE notification_id = 'real-notification'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM payments")).isEqualTo(31);
    }

    private void insertRealRows() {
        jdbc.update("""
                INSERT INTO payments
                  (id, order_id, customer_id, provider, status, amount, currency,
                   idempotency_key, created_at, updated_at, version)
                VALUES (999999999, 888888888, 'real-customer', 'MERCADO_PAGO', 'PENDING',
                        10.00, 'ARS', 'real-key', now(), now(), 0)
                """);
        jdbc.update("""
                INSERT INTO payment_events
                  (provider, notification_id, resource_type, resource_id, signature_valid,
                   status, attempts, created_at)
                VALUES ('MERCADO_PAGO', 'real-notification', 'payment', 'real-resource', true,
                        'RECEIVED', 0, now())
                """);
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
