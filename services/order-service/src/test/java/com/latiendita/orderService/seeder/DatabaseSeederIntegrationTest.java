package com.latiendita.orderService.seeder;

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

    private static final String ORDER_PATTERN = "SEED-ORD-%";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseSeederService service;

    @Autowired
    private SeederProperties properties;

    @Autowired
    private SeederValidator validator;

    @Autowired
    private OrderDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealOrder();
        int expectedEvents = generator.generate().events().size();
        int expectedItems = generator.generate().items().size();

        SeedExecution first = service.seed();
        assertThat(first.executed()).isTrue();
        assertThat(first.orders()).isEqualTo(30);
        assertThat(first.items()).isEqualTo(expectedItems);
        assertThat(first.events()).isEqualTo(expectedEvents);
        assertThat(count("SELECT count(*) FROM orders WHERE order_number LIKE ?", ORDER_PATTERN))
                .isEqualTo(30);
        assertThat(count("SELECT count(DISTINCT order_number) FROM orders "
                + "WHERE order_number LIKE ?", ORDER_PATTERN)).isEqualTo(30);
        assertThat(count("""
                SELECT count(*) FROM order_items i
                JOIN orders o ON o.id = i.order_id
                WHERE o.order_number LIKE ?
                """, ORDER_PATTERN)).isEqualTo(expectedItems);
        assertThat(count("""
                SELECT count(*) FROM (
                    SELECT o.id FROM orders o JOIN order_items i ON i.order_id = o.id
                    WHERE o.order_number LIKE ?
                    GROUP BY o.id, o.total HAVING o.total <> SUM(i.subtotal)
                ) mismatches
                """, ORDER_PATTERN)).isZero();
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'order' "
                + "AND version = 'order-v1'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM orders WHERE order_number = 'real-order'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM orders")).isEqualTo(31);

        SeedExecution second = service.seed();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM orders WHERE order_number LIKE ?", ORDER_PATTERN))
                .isEqualTo(30);

        jdbc.update("DELETE FROM order_items WHERE order_id = "
                + "(SELECT id FROM orders WHERE order_number = 'SEED-ORD-9200001')");
        assertThatThrownBy(() -> validator.validate(generator.generate(),
                DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ítems sintéticos");

        jdbc.update("DELETE FROM seeder_markers WHERE service = 'order'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.seed();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM orders WHERE order_number LIKE ?", ORDER_PATTERN))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM order_items i JOIN orders o ON o.id = i.order_id "
                + "WHERE o.order_number LIKE ?", ORDER_PATTERN)).isEqualTo(expectedItems);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?",
                "%\"source\":\"SEED\"%")).isEqualTo(expectedEvents);
        assertThat(count("SELECT count(*) FROM orders WHERE order_number = 'real-order'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM orders")).isEqualTo(31);
    }

    private void insertRealOrder() {
        jdbc.update("""
                INSERT INTO orders
                  (id, order_number, customer_id, status, total, refund_required,
                   created_at, updated_at, version)
                VALUES (999999999, 'real-order', 'real-customer', 'PENDING', 10.00,
                        false, now(), now(), 0)
                """);
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
