package com.latiendita.shipmentService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.latiendita.shipmentService.seeder.model.SeedDataset;
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
        "seeder.count=20",
        "seeder.batch-size=10",
        "seeder.random-seed=99",
        "seeder.include-infra=true",
        "seeder.external-id-prefix=SEED",
        "seeder.external-id-base=9000000"
})
@ActiveProfiles("test")
class DatabaseSeederIntegrationTest {

    private static final String CUSTOMER_PATTERN = "SEED-CUST-%";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseSeederService service;

    @Autowired
    private SeederProperties properties;

    @Autowired
    private SeederValidator validator;

    @Autowired
    private ShipmentDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealShipment();
        SeedDataset expected = generator.generate();

        SeedExecution first = service.seed();
        assertThat(first.executed()).isTrue();
        assertThat(first.shipments()).isEqualTo(20);
        assertThat(first.addresses()).isEqualTo(20);
        assertThat(first.events()).isEqualTo(expected.events().size());
        assertThat(countShipments()).isEqualTo(21);
        assertThat(count("SELECT count(*) FROM shipments WHERE customer_id LIKE ?",
                CUSTOMER_PATTERN)).isEqualTo(20);
        assertThat(count("SELECT count(*) FROM delivery_addresses WHERE customer_id LIKE ?",
                CUSTOMER_PATTERN)).isEqualTo(20);
        assertThat(count("""
                SELECT count(*) FROM outbox_events WHERE topic = 'shipment-events'
                AND payload LIKE ?
                """, "%\"source\":\"SEED\"%")).isEqualTo(expected.events().size());
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'shipment' "
                + "AND version = 'shipment-v1'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM shipments WHERE customer_id = 'real-customer'"))
                .isEqualTo(1);

        SeedExecution second = service.seed();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(countShipments()).isEqualTo(21);

        jdbc.update("DELETE FROM delivery_addresses WHERE customer_id = ?",
                expected.addresses().get(0).customerId());
        assertThatThrownBy(() -> validator.validate(generator.generate(),
                DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("direcciones sintéticas");

        jdbc.update("DELETE FROM seeder_markers WHERE service = 'shipment'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.seed();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM delivery_addresses WHERE customer_id LIKE ?",
                CUSTOMER_PATTERN)).isEqualTo(20);
        assertThat(count("""
                SELECT count(*) FROM outbox_events WHERE topic = 'shipment-events'
                AND payload LIKE ?
                """, "%\"source\":\"SEED\"%")).isEqualTo(expected.events().size());
        assertThat(count("SELECT count(*) FROM shipments WHERE customer_id = 'real-customer'"))
                .isEqualTo(1);
        assertThat(countShipments()).isEqualTo(21);
    }

    private void insertRealShipment() {
        jdbc.update("""
                INSERT INTO shipments
                  (id, order_id, customer_id, status, shipping_cost, currency,
                   recipient_name, phone, address_line, city, province, postal_code,
                   estimated_from, estimated_to, created_at, updated_at, version)
                VALUES (999999999, 999999999, 'real-customer', 'PENDING_DISPATCH', 10.00,
                        'ARS', 'Real', '123456789', 'Calle real 1', 'Buenos Aires', 'CABA',
                        '1000', DATE '2024-01-01', DATE '2024-01-05', now(), now(), 0)
                """);
    }

    private long countShipments() {
        return count("SELECT count(*) FROM shipments");
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
