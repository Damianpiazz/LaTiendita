package com.latiendita.shipmentService.seeder;

import com.latiendita.shipmentService.seeder.model.SeededAddress;
import com.latiendita.shipmentService.seeder.model.SeededOutboxEvent;
import com.latiendita.shipmentService.seeder.model.SeededShipment;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** Acceso JDBC directo: inserciones por batch y marcador sintético. */
@Repository
public class SeederJdbcRepository {

    static final String SERVICE = "shipment";

    private static final String INSERT_SHIPMENT = """
            INSERT INTO shipments
              (id, order_id, customer_id, status, shipping_cost, currency,
               recipient_name, phone, address_line, city, province, postal_code,
               reference_notes, estimated_from, estimated_to, dispatched_at,
               delivered_at, created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_ADDRESS = """
            INSERT INTO delivery_addresses
              (id, customer_id, recipient_name, phone, address_line, city, province,
               postal_code, reference_notes, is_default, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
        String customerPattern = customerPattern();
        transaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM outbox_events WHERE payload LIKE ?",
                    sourcePattern(properties.getExternalIdPrefix()));
            jdbc.update("DELETE FROM delivery_addresses WHERE customer_id LIKE ?", customerPattern);
            jdbc.update("DELETE FROM shipments WHERE customer_id LIKE ?", customerPattern);
            deleteMarkers();
        });
    }

    public int insertShipments(List<SeededShipment> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_SHIPMENT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededShipment row) -> {
                    ps.setLong(1, row.id());
                    ps.setLong(2, row.orderId());
                    ps.setString(3, row.customerId());
                    ps.setString(4, row.status());
                    ps.setBigDecimal(5, row.shippingCost());
                    ps.setString(6, row.currency());
                    ps.setString(7, row.recipientName());
                    ps.setString(8, row.phone());
                    ps.setString(9, row.addressLine());
                    ps.setString(10, row.city());
                    ps.setString(11, row.province());
                    ps.setString(12, row.postalCode());
                    ps.setString(13, row.references());
                    setLocalDate(ps, 14, row.estimatedFrom());
                    setLocalDate(ps, 15, row.estimatedTo());
                    setInstant(ps, 16, row.dispatchedAt());
                    setInstant(ps, 17, row.deliveredAt());
                    setInstant(ps, 18, row.createdAt());
                    setInstant(ps, 19, row.updatedAt());
                    ps.setLong(20, row.version());
                });
        return rows.size();
    }

    public int insertAddresses(List<SeededAddress> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_ADDRESS, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededAddress row) -> {
                    ps.setLong(1, row.id());
                    ps.setString(2, row.customerId());
                    ps.setString(3, row.recipientName());
                    ps.setString(4, row.phone());
                    ps.setString(5, row.addressLine());
                    ps.setString(6, row.city());
                    ps.setString(7, row.province());
                    ps.setString(8, row.postalCode());
                    ps.setString(9, row.references());
                    ps.setBoolean(10, row.isDefault());
                    setInstant(ps, 11, row.createdAt());
                    setInstant(ps, 12, row.updatedAt());
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

    private String customerPattern() {
        return properties.getExternalIdPrefix() + "-CUST-%";
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

    private static void setLocalDate(PreparedStatement ps, int index, LocalDate value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setObject(index, value);
        }
    }
}
