package com.latiendita.offerService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.latiendita.offerService.seeder.model.SeedDataset;
import java.sql.Timestamp;
import java.time.Instant;
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
        "seeder.external-id-prefix=SEED",
        "seeder.external-id-base=9000000"
})
@ActiveProfiles("test")
class DatabaseSeederIntegrationTest {

    private static final String PROMO_PATTERN = "SEED-PROMO-%";
    private static final String USAGE_PATTERN = "SEED-CUST-%";
    private static final String BENEFIT_PATTERN = "SEED-BENEFIT-%";
    private static final String EVENT_PATTERN = "%\"source\":\"SEED\"%";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseSeederService service;

    @Autowired
    private SeederProperties properties;

    @Autowired
    private SeederValidator validator;

    @Autowired
    private OfferDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealRows();
        int expectedEvents = generator.generate().events().size();

        SeedExecution first = service.seed();
        assertThat(first.executed()).isTrue();
        assertThat(first.promotions()).isEqualTo(30);
        assertThat(first.usages()).isEqualTo(30);
        assertThat(first.benefits()).isEqualTo(20);
        assertThat(first.outbox()).isEqualTo(expectedEvents);
        assertThat(count("SELECT count(*) FROM promotions WHERE code LIKE ?", PROMO_PATTERN))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM coupon_usages WHERE customer_sub LIKE ?",
                USAGE_PATTERN)).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM benefits WHERE code LIKE ?", BENEFIT_PATTERN))
                .isEqualTo(20);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?", EVENT_PATTERN))
                .isEqualTo(expectedEvents);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ? "
                + "AND status='SENT' AND sent_at IS NOT NULL", EVENT_PATTERN))
                .isEqualTo(expectedEvents);
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service='offer' "
                + "AND version='offer-v1'")).isEqualTo(1);

        assertThat(count("SELECT count(*) FROM promotions WHERE code='REAL-PROMO'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM benefits WHERE code='REAL-BENEFIT'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM promotions")).isEqualTo(31);
        assertThat(count("SELECT count(*) FROM benefits")).isEqualTo(21);

        SeedExecution second = service.seed();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM promotions WHERE code LIKE ?", PROMO_PATTERN))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?", EVENT_PATTERN))
                .isEqualTo(expectedEvents);

        SeedDataset dataset = generator.generate();
        jdbc.update("DELETE FROM outbox_events WHERE aggregate_id = ? AND payload LIKE ?",
                dataset.events().get(0).aggregateId(), EVENT_PATTERN);
        assertThatThrownBy(() -> validator.validate(dataset, DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eventos outbox");

        jdbc.update("DELETE FROM seeder_markers WHERE service='offer'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.seed();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM promotions WHERE code LIKE ?", PROMO_PATTERN))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM coupon_usages WHERE customer_sub LIKE ?",
                USAGE_PATTERN)).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM benefits WHERE code LIKE ?", BENEFIT_PATTERN))
                .isEqualTo(20);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?", EVENT_PATTERN))
                .isEqualTo(expectedEvents);
        assertThat(count("SELECT count(*) FROM promotions WHERE code='REAL-PROMO'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM benefits WHERE code='REAL-BENEFIT'")).isEqualTo(1);
    }

    private void insertRealRows() {
        Instant now = Instant.now();
        jdbc.update("""
                INSERT INTO promotions
                  (id, code, name, type, scope, starts_at, ends_at, priority,
                   stackable, active, created_at, updated_at)
                VALUES (?, 'REAL-PROMO', 'Promoción real', 'FEATURED', 'ORDER',
                        ?, ?, 1, true, true, ?, ?)
                """, UUID.fromString("88888888-0000-0000-0000-000000000001"),
                Timestamp.from(now.minusSeconds(3600)), Timestamp.from(now.plusSeconds(3600)),
                Timestamp.from(now), Timestamp.from(now));
        jdbc.update("""
                INSERT INTO benefits
                  (id, code, title, description, icon, sort_order, active, created_at, updated_at)
                VALUES (?, 'REAL-BENEFIT', 'Beneficio real', 'No borrar', 'icon-real',
                        99, true, ?, ?)
                """, UUID.fromString("88888888-0000-0000-0000-000000000002"),
                Timestamp.from(now), Timestamp.from(now));
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
