package com.latiendita.paymentService.seeder;

import com.latiendita.paymentService.seeder.model.SeededPayment;
import com.latiendita.paymentService.seeder.model.SeededPaymentEvent;
import com.latiendita.paymentService.seeder.model.SeededOutboxEvent;
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

    static final String SERVICE = "payment";

    private static final String INSERT_PAYMENT = """
            INSERT INTO payments
              (id, order_id, customer_id, provider, status, amount, currency,
               preference_id, checkout_url, external_reference, idempotency_key,
               provider_payment_id, payment_method_id, payment_type, status_detail,
               paid_at, expires_at, created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_EVENT = """
            INSERT INTO payment_events
              (provider, notification_id, resource_type, resource_id, action, signature_valid,
               status, attempts, last_error, created_at, processed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_OUTBOX = """
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
        transaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM outbox_events WHERE payload LIKE ?", sourcePattern(prefix));
            jdbc.update("DELETE FROM payment_events WHERE notification_id LIKE ?",
                    prefix + "-NOTIF-%");
            jdbc.update("DELETE FROM payments WHERE idempotency_key LIKE ?",
                    prefix + "-IDEM-%");
            deleteMarkers();
        });
    }

    public int insertPayments(List<SeededPayment> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_PAYMENT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededPayment row) -> {
                    ps.setLong(1, row.id());
                    ps.setLong(2, row.orderId());
                    ps.setString(3, row.customerId());
                    ps.setString(4, row.provider());
                    ps.setString(5, row.status());
                    ps.setBigDecimal(6, row.amount());
                    ps.setString(7, row.currency());
                    ps.setString(8, row.preferenceId());
                    ps.setString(9, row.checkoutUrl());
                    ps.setString(10, row.externalReference());
                    ps.setString(11, row.idempotencyKey());
                    ps.setString(12, row.providerPaymentId());
                    ps.setString(13, row.paymentMethodId());
                    ps.setString(14, row.paymentType());
                    ps.setString(15, row.statusDetail());
                    setInstant(ps, 16, row.paidAt());
                    setInstant(ps, 17, row.expiresAt());
                    setInstant(ps, 18, row.createdAt());
                    setInstant(ps, 19, row.updatedAt());
                    ps.setLong(20, row.version());
                });
        return rows.size();
    }

    public int insertEvents(List<SeededPaymentEvent> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_EVENT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededPaymentEvent row) -> {
                    ps.setString(1, row.provider());
                    ps.setString(2, row.notificationId());
                    ps.setString(3, row.resourceType());
                    ps.setString(4, row.resourceId());
                    ps.setString(5, row.action());
                    ps.setBoolean(6, row.signatureValid());
                    ps.setString(7, row.status());
                    ps.setInt(8, row.attempts());
                    ps.setString(9, row.lastError());
                    setInstant(ps, 10, row.createdAt());
                    setInstant(ps, 11, row.processedAt());
                });
        return rows.size();
    }

    /** Reemplaza los eventos sintéticos para que un rerun sin marcador no duplique eventos. */
    public int replaceSeededOutbox(String prefix, List<SeededOutboxEvent> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.update("DELETE FROM outbox_events WHERE payload LIKE ?", sourcePattern(prefix));
        jdbc.batchUpdate(INSERT_OUTBOX, rows, properties.getBatchSize(),
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
