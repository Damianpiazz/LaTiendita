package com.latiendita.reviewService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.latiendita.reviewService.seeder.model.SeedDataset;
import java.math.BigDecimal;
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
        "seeder.external-id-base=9000000",
        "seeder.include-infra=true"
})
@ActiveProfiles("test")
class DatabaseSeederIntegrationTest {

    private static final long ORDER_START = 9_200_000L;
    private static final long ORDER_END = 9_200_029L;
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
    private ReviewDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealRows();
        SeedDataset expected = generator.generate();

        SeedExecution first = service.seed();
        assertThat(first.executed()).isTrue();
        assertThat(first.reviews()).isEqualTo(30);
        assertThat(first.votes()).isEqualTo(expected.votes().size());
        assertThat(first.ratings()).isEqualTo(expected.ratings().size());
        assertThat(first.outbox()).isEqualTo(expected.events().size());
        assertThat(count("SELECT count(*) FROM reviews WHERE order_id BETWEEN ? AND ?",
                ORDER_START, ORDER_END)).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM review_votes v "
                + "JOIN reviews r ON r.id=v.review_id WHERE r.order_id BETWEEN ? AND ?",
                ORDER_START, ORDER_END)).isEqualTo(expected.votes().size());
        assertThat(count("SELECT count(*) FROM product_ratings WHERE product_id BETWEEN ? AND ?",
                9_100_000L, 9_100_029L)).isEqualTo(expected.ratings().size());
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?",
                EVENT_PATTERN)).isEqualTo(expected.events().size());
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ? "
                + "AND status='SENT' AND sent_at IS NOT NULL AND attempts=1",
                EVENT_PATTERN)).isEqualTo(expected.events().size());
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service='review' "
                + "AND version='review-v1'")).isEqualTo(1);

        assertThat(count("SELECT count(*) FROM reviews WHERE customer_id='REAL-CUST'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM product_ratings WHERE product_id=7770001"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE aggregate_id='REAL-EVENT'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM reviews")).isEqualTo(31);
        assertThat(count("SELECT count(*) FROM outbox_events"))
                .isEqualTo(expected.events().size() + 1);

        SeedExecution second = service.seed();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM reviews WHERE order_id BETWEEN ? AND ?",
                ORDER_START, ORDER_END)).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?",
                EVENT_PATTERN)).isEqualTo(expected.events().size());

        jdbc.update("DELETE FROM outbox_events WHERE aggregate_id = ? AND payload LIKE ?",
                expected.events().get(0).aggregateId(), EVENT_PATTERN);
        assertThatThrownBy(() -> validator.validate(expected, DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eventos outbox");

        jdbc.update("DELETE FROM seeder_markers WHERE service='review'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.seed();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM reviews WHERE order_id BETWEEN ? AND ?",
                ORDER_START, ORDER_END)).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM review_votes v "
                + "JOIN reviews r ON r.id=v.review_id WHERE r.order_id BETWEEN ? AND ?",
                ORDER_START, ORDER_END)).isEqualTo(expected.votes().size());
        assertThat(count("SELECT count(*) FROM outbox_events WHERE payload LIKE ?",
                EVENT_PATTERN)).isEqualTo(expected.events().size());
        assertThat(count("SELECT count(*) FROM reviews WHERE customer_id='REAL-CUST'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM product_ratings WHERE product_id=7770001"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM outbox_events WHERE aggregate_id='REAL-EVENT'"))
                .isEqualTo(1);
    }

    private void insertRealRows() {
        Instant now = Instant.now();
        jdbc.update("""
                INSERT INTO reviews
                  (id, product_id, customer_id, order_id, rating, title, body, status,
                   helpful_votes, moderation_reason, created_at, updated_at)
                VALUES (?, 7770001, 'REAL-CUST', 7770000, 5, 'Reseña real', 'No borrar',
                        'approved', 0, NULL, ?, ?)
                """, UUID.fromString("88888888-0000-0000-0000-000000000001"),
                Timestamp.from(now), Timestamp.from(now));
        jdbc.update("""
                INSERT INTO product_ratings
                  (product_id, average, review_count, distribution, updated_at)
                VALUES (7770001, ?, 1, CAST(? AS jsonb), ?)
                """, BigDecimal.valueOf(5).setScale(2),
                "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":1}", Timestamp.from(now));
        jdbc.update("""
                INSERT INTO outbox_events
                  (topic, aggregate_id, event_type, payload, status, attempts,
                   last_error, created_at, sent_at)
                VALUES ('review-events', 'REAL-EVENT', 'REVIEW_APPROVED',
                        '{"type":"REVIEW_APPROVED","source":"REAL"}', 'SENT', 1,
                        NULL, ?, ?)
                """, Timestamp.from(now), Timestamp.from(now));
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
