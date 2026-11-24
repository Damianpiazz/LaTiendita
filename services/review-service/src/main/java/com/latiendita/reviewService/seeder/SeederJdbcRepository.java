package com.latiendita.reviewService.seeder;

import com.latiendita.reviewService.seeder.model.SeededOutboxEvent;
import com.latiendita.reviewService.seeder.model.SeededProductRating;
import com.latiendita.reviewService.seeder.model.SeededReview;
import com.latiendita.reviewService.seeder.model.SeededVote;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** Acceso JDBC directo: inserciones por batch, limpieza controlada y marcador. */
@Repository
public class SeederJdbcRepository {

    static final String SERVICE = "review";
    static final String EVENT_PATTERN = "%\"source\":\"SEED\"%";
    static final long PRODUCT_OFFSET = 100_000L;
    static final long ORDER_OFFSET = 200_000L;

    private static final String INSERT_REVIEW = """
            INSERT INTO reviews
              (id, product_id, customer_id, order_id, rating, title, body, status,
               helpful_votes, moderation_reason, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_VOTE = """
            INSERT INTO review_votes (id, review_id, customer_id, created_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_RATING = """
            INSERT INTO product_ratings
              (product_id, average, review_count, distribution, updated_at)
            VALUES (?, ?, ?, CAST(? AS jsonb), ?)
            ON CONFLICT (product_id) DO NOTHING
            """;

    private static final String INSERT_EVENT = """
            INSERT INTO outbox_events
              (topic, aggregate_id, event_type, payload, status, attempts,
               last_error, created_at, sent_at)
            VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?)
            """;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final SeederProperties properties;

    public SeederJdbcRepository(JdbcTemplate jdbc, TransactionTemplate transaction,
                                SeederProperties properties) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.properties = properties;
    }

    public void ensureMarkerTable() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS seeder_markers (
                    service       varchar(64)  NOT NULL,
                    version       varchar(64)  NOT NULL,
                    rows_seeded   bigint       NOT NULL,
                    details       text,
                    completed_at  timestamptz  NOT NULL DEFAULT now(),
                    PRIMARY KEY (service, version)
                )
                """);
    }

    public boolean markerExists(String version) {
        if (!properties.isMarkerEnabled()) {
            return false;
        }
        return countWhere("SELECT count(*) FROM seeder_markers WHERE service = ? AND version = ?",
                SERVICE, version) > 0;
    }

    public void writeMarker(String version, long rows, String details) {
        jdbc.update("""
                INSERT INTO seeder_markers (service, version, rows_seeded, details)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (service, version) DO NOTHING
                """, SERVICE, version, rows, details);
    }

    public boolean verifyMarker(String version) {
        return countWhere("SELECT count(*) FROM seeder_markers WHERE service = ? AND version = ?",
                SERVICE, version) == 1;
    }

    public void deleteMarkers() {
        jdbc.update("DELETE FROM seeder_markers WHERE service = ?", SERVICE);
    }

    public long countWhere(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    /** Limpieza controlada: borra sólo filas sintéticas y el marcador. */
    public void cleanSeededRows() {
        long base = properties.getExternalIdBase();
        long productStart = base + PRODUCT_OFFSET;
        long productEnd = productStart + properties.getCount() - 1;
        long orderStart = base + ORDER_OFFSET;
        long orderEnd = orderStart + properties.getCount() - 1;
        transaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM review_votes WHERE review_id IN ("
                    + "SELECT id FROM reviews WHERE order_id BETWEEN ? AND ?)",
                    orderStart, orderEnd);
            jdbc.update("DELETE FROM outbox_events WHERE payload LIKE ?", EVENT_PATTERN);
            jdbc.update("DELETE FROM reviews WHERE order_id BETWEEN ? AND ?",
                    orderStart, orderEnd);
            jdbc.update("DELETE FROM product_ratings WHERE product_id BETWEEN ? AND ?",
                    productStart, productEnd);
            deleteMarkers();
        });
    }

    public int insertReviews(List<SeededReview> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_REVIEW, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededReview row) -> {
                    ps.setObject(1, row.id());
                    ps.setLong(2, row.productId());
                    ps.setString(3, row.customerId());
                    ps.setLong(4, row.orderId());
                    ps.setInt(5, row.rating());
                    ps.setString(6, row.title());
                    ps.setString(7, row.body());
                    ps.setString(8, row.status());
                    ps.setInt(9, row.helpfulVotes());
                    ps.setString(10, row.moderationReason());
                    ps.setTimestamp(11, Timestamp.from(row.createdAt()));
                    ps.setTimestamp(12, Timestamp.from(row.updatedAt()));
                });
        return rows.size();
    }

    public int insertVotes(List<SeededVote> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_VOTE, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededVote row) -> {
                    ps.setObject(1, row.id());
                    ps.setObject(2, row.reviewId());
                    ps.setString(3, row.customerId());
                    ps.setTimestamp(4, Timestamp.from(row.createdAt()));
                });
        return rows.size();
    }

    public int insertRatings(List<SeededProductRating> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_RATING, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededProductRating row) -> {
                    ps.setLong(1, row.productId());
                    ps.setBigDecimal(2, row.average());
                    ps.setInt(3, row.reviewCount());
                    ps.setString(4, row.distribution());
                    ps.setTimestamp(5, Timestamp.from(row.updatedAt()));
                });
        return rows.size();
    }

    public int insertEvents(List<SeededOutboxEvent> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_EVENT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededOutboxEvent row) -> {
                    ps.setString(1, row.topic());
                    ps.setString(2, row.aggregateId());
                    ps.setString(3, row.eventType());
                    ps.setString(4, row.payload());
                    ps.setString(5, row.status());
                    ps.setInt(6, row.attempts());
                    ps.setTimestamp(7, Timestamp.from(row.createdAt()));
                    ps.setTimestamp(8, Timestamp.from(row.sentAt()));
                });
        return rows.size();
    }
}
