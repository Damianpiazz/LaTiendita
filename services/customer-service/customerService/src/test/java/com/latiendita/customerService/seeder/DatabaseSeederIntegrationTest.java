package com.latiendita.customerService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
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
    private CustomerDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealCustomer();

        SeedExecution first = service.run();
        assertThat(first.executed()).isTrue();
        assertThat(first.customers()).isEqualTo(30);
        assertThat(first.events()).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM customers WHERE authorizer_sub LIKE 'SEED-SUB-%'"))
                .isEqualTo(30);
        assertThat(count("SELECT count(DISTINCT email) FROM customers "
                + "WHERE email LIKE 'seed+%@example.test'")).isEqualTo(30);
        assertThat(count("""
                SELECT count(*) FROM outbox_events e
                JOIN customers c ON CAST(c.id AS varchar(64)) = e.aggregate_id
                WHERE c.authorizer_sub LIKE 'SEED-SUB-%'
                  AND e.topic = 'customer-events'
                  AND e.event_type = 'CUSTOMER_CREATED'
                  AND e.status = 'SENT' AND e.sent_at IS NOT NULL AND e.attempts = 1
                """)).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'customer' "
                + "AND version = 'customer-v1'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM customers WHERE authorizer_sub = 'real-sub'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM customers")).isEqualTo(31);

        SeedExecution second = service.run();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM customers WHERE authorizer_sub LIKE 'SEED-SUB-%'"))
                .isEqualTo(30);

        jdbc.update("DELETE FROM customers WHERE authorizer_sub = ?", "SEED-SUB-000001");
        assertThatThrownBy(() -> validator.validate(generator.generate(),
                DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("clientes sintéticos");

        jdbc.update("DELETE FROM seeder_markers WHERE service = 'customer'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.run();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM customers WHERE authorizer_sub LIKE 'SEED-SUB-%'"))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE '%\"source\":\"SEED\"%'"))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM customers WHERE authorizer_sub = 'real-sub'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM customers")).isEqualTo(31);
    }

    private void insertRealCustomer() {
        jdbc.update("""
                INSERT INTO customers
                  (id, authorizer_sub, email, first_name, last_name, phone, status,
                   newsletter_opt_in, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', false, now(), now())
                """, UUID.randomUUID(), "real-sub", "real@example.test", "Real", "Customer",
                "600000001");
    }

    private long count(String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
