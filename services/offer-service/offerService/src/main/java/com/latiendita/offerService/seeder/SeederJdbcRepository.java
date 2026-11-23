package com.latiendita.offerService.seeder;

import com.latiendita.offerService.seeder.model.SeededBenefit;
import com.latiendita.offerService.seeder.model.SeededCouponUsage;
import com.latiendita.offerService.seeder.model.SeededOutboxEvent;
import com.latiendita.offerService.seeder.model.SeededPromotion;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** Acceso JDBC directo: inserciones por batch, limpieza controlada y marcador. */
@Repository
public class SeederJdbcRepository {

    static final String SERVICE = "offer";

    private static final String INSERT_PROMOTION = """
            INSERT INTO promotions
              (id, code, name, type, scope, product_id, category_id, discount_value,
               min_subtotal, starts_at, ends_at, priority, stackable, active,
               created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_USAGE = """
            INSERT INTO coupon_usages (id, promotion_id, customer_sub, order_id, used_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_BENEFIT = """
            INSERT INTO benefits
              (id, code, title, description, icon, sort_order, active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
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

    /** Limpieza controlada: borra sólo las filas sintéticas y el marcador. */
    public void cleanSeededRows() {
        String prefix = properties.getExternalIdPrefix();
        transaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM coupon_usages WHERE customer_sub LIKE ?",
                    prefix + "-CUST-%");
            jdbc.update("DELETE FROM outbox_events WHERE payload LIKE ?",
                    "%\"source\":\"SEED\"%");
            jdbc.update("DELETE FROM promotions WHERE code LIKE ?", prefix + "-PROMO-%");
            jdbc.update("DELETE FROM benefits WHERE code LIKE ?", prefix + "-BENEFIT-%");
            deleteMarkers();
        });
    }

    public int insertPromotions(List<SeededPromotion> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_PROMOTION, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededPromotion row) -> {
                    ps.setObject(1, row.id());
                    ps.setString(2, row.code());
                    ps.setString(3, row.name());
                    ps.setString(4, row.type());
                    ps.setString(5, row.scope());
                    setNullableLong(ps, 6, row.productId());
                    setNullableLong(ps, 7, row.categoryId());
                    setNullableDecimal(ps, 8, row.discountValue());
                    setNullableDecimal(ps, 9, row.minSubtotal());
                    ps.setTimestamp(10, Timestamp.from(row.startsAt()));
                    setNullableInstant(ps, 11, row.endsAt());
                    ps.setInt(12, row.priority());
                    ps.setBoolean(13, row.stackable());
                    ps.setBoolean(14, row.active());
                    ps.setTimestamp(15, Timestamp.from(row.createdAt()));
                    ps.setTimestamp(16, Timestamp.from(row.updatedAt()));
                });
        return rows.size();
    }

    public int insertUsages(List<SeededCouponUsage> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_USAGE, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededCouponUsage row) -> {
                    ps.setObject(1, row.id());
                    ps.setObject(2, row.promotionId());
                    ps.setString(3, row.customerSub());
                    ps.setLong(4, row.orderId());
                    ps.setTimestamp(5, Timestamp.from(row.usedAt()));
                });
        return rows.size();
    }

    public int insertBenefits(List<SeededBenefit> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_BENEFIT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededBenefit row) -> {
                    ps.setObject(1, row.id());
                    ps.setString(2, row.code());
                    ps.setString(3, row.title());
                    ps.setString(4, row.description());
                    ps.setString(5, row.icon());
                    ps.setInt(6, row.sortOrder());
                    ps.setBoolean(7, row.active());
                    ps.setTimestamp(8, Timestamp.from(row.createdAt()));
                    ps.setTimestamp(9, Timestamp.from(row.updatedAt()));
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

    private static void setNullableLong(PreparedStatement ps, int index, Long value)
            throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private static void setNullableDecimal(PreparedStatement ps, int index, java.math.BigDecimal value)
            throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.NUMERIC);
        } else {
            ps.setBigDecimal(index, value);
        }
    }

    private static void setNullableInstant(PreparedStatement ps, int index, java.time.Instant value)
            throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.from(value));
        }
    }
}
