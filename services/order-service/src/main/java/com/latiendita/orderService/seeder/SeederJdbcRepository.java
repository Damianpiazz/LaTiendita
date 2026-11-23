package com.latiendita.orderService.seeder;

import com.latiendita.orderService.seeder.model.SeededOrder;
import com.latiendita.orderService.seeder.model.SeededOrderItem;
import com.latiendita.orderService.seeder.model.SeededOutboxEvent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** Acceso JDBC directo: inserciones por batch y marcador sintético. */
@Repository
public class SeederJdbcRepository {

    static final String SERVICE = "order";

    private static final String INSERT_ORDER = """
            INSERT INTO orders
              (id, order_number, customer_id, status, total, cancel_reason,
               refund_required, created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_ITEM = """
            INSERT INTO order_items
              (id, order_id, product_id, product_title, sku, quantity, unit_price, subtotal)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_EVENT = """
            INSERT INTO outbox_events
              (topic, aggregate_id, event_type, payload, status, attempts,
               last_error, created_at, sent_at)
            VALUES (?, ?, ?, ?, 'SENT', 1, NULL, ?, ?)
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
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM seeder_markers WHERE service = ? AND version = ?",
                Integer.class, SERVICE, version);
        return count != null && count > 0;
    }

    public void writeMarker(String version, long rows, String details) {
        jdbc.update("""
                INSERT INTO seeder_markers (service, version, rows_seeded, details)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (service, version) DO NOTHING
                """, SERVICE, version, rows, details);
    }

    public boolean verifyMarker(String version) {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM seeder_markers WHERE service = ? AND version = ?",
                Integer.class, SERVICE, version);
        return count != null && count == 1;
    }

    public void deleteMarkers() {
        jdbc.update("DELETE FROM seeder_markers WHERE service = ?", SERVICE);
    }

    public long countWhere(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    /** Limpieza controlada: borra sólo filas con el prefijo sintético del seeder. */
    public void cleanSeededRows() {
        String prefix = properties.getExternalIdPrefix();
        String orderPattern = prefix + "-ORD-%";
        transaction.executeWithoutResult(status -> {
            jdbc.update("""
                    DELETE FROM outbox_events
                    WHERE payload LIKE ? OR aggregate_id IN (
                        SELECT CAST(id AS varchar(64)) FROM orders WHERE order_number LIKE ?
                    )
                    """, sourcePattern(prefix), orderPattern);
            jdbc.update("""
                    DELETE FROM order_items
                    WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE ?)
                    """, orderPattern);
            jdbc.update("DELETE FROM orders WHERE order_number LIKE ?", orderPattern);
            deleteMarkers();
        });
    }

    public int insertOrders(List<SeededOrder> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_ORDER, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededOrder row) -> {
                    ps.setLong(1, row.id());
                    ps.setString(2, row.orderNumber());
                    ps.setString(3, row.customerId());
                    ps.setString(4, row.status());
                    ps.setBigDecimal(5, row.total());
                    ps.setString(6, row.cancelReason());
                    ps.setBoolean(7, row.refundRequired());
                    setInstant(ps, 8, row.createdAt());
                    setInstant(ps, 9, row.updatedAt());
                    ps.setLong(10, row.version());
                });
        return rows.size();
    }

    public int insertItems(List<SeededOrderItem> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_ITEM, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededOrderItem row) -> {
                    ps.setLong(1, row.id());
                    ps.setLong(2, row.orderId());
                    ps.setLong(3, row.productId());
                    ps.setString(4, row.productTitle());
                    ps.setString(5, row.sku());
                    ps.setInt(6, row.quantity());
                    ps.setBigDecimal(7, row.unitPrice());
                    ps.setBigDecimal(8, row.subtotal());
                });
        return rows.size();
    }

    /** Reemplaza los eventos sintéticos para que un rerun sin marcador no duplique eventos. */
    public int replaceSeededEvents(String prefix, List<SeededOutboxEvent> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.update("DELETE FROM outbox_events WHERE payload LIKE ?", sourcePattern(prefix));
        jdbc.batchUpdate(INSERT_EVENT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededOutboxEvent row) -> {
                    ps.setString(1, row.topic());
                    ps.setString(2, row.aggregateId());
                    ps.setString(3, row.eventType());
                    ps.setString(4, row.payload());
                    setInstant(ps, 5, row.createdAt());
                    setInstant(ps, 6, row.sentAt());
                });
        return rows.size();
    }

    private static String sourcePattern(String prefix) {
        return "%\"source\":\"" + prefix + "\"%";
    }

    private static void setInstant(PreparedStatement ps, int index, Instant value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.from(value));
        }
    }
}
