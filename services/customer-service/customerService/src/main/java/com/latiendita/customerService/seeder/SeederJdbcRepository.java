package com.latiendita.customerService.seeder;

import com.latiendita.customerService.seeder.model.SeededCustomer;
import com.latiendita.customerService.seeder.model.SeededOutboxEvent;
import java.sql.PreparedStatement;
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

    static final String SERVICE = "customer";

    private static final String INSERT_CUSTOMER = """
            INSERT INTO customers
              (id, authorizer_sub, email, first_name, last_name, phone, status,
               newsletter_opt_in, newsletter_consent_at, last_login_at,
               created_at, updated_at, deleted_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)
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

    public long count(String table) {
        Long value = jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
        return value == null ? 0L : value;
    }

    public long countWhere(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    /** Limpieza controlada: borra sólo filas con el prefijo sintético del seeder. */
    public void cleanSeededRows() {
        String prefix = properties.getExternalIdPrefix();
        transaction.executeWithoutResult(status -> {
            jdbc.update("""
                    DELETE FROM outbox_events
                    WHERE payload LIKE ? OR aggregate_id IN (
                        SELECT CAST(id AS varchar(64)) FROM customers WHERE authorizer_sub LIKE ?
                    )
                    """, sourcePattern(prefix), prefix + "-SUB-%");
            jdbc.update("DELETE FROM customers WHERE authorizer_sub LIKE ?",
                    prefix + "-SUB-%");
            deleteMarkers();
        });
    }

    public int insertCustomers(List<SeededCustomer> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_CUSTOMER, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededCustomer row) -> {
                    ps.setObject(1, row.id());
                    ps.setString(2, row.authorizerSub());
                    ps.setString(3, row.email());
                    ps.setString(4, row.firstName());
                    ps.setString(5, row.lastName());
                    ps.setString(6, row.phone());
                    ps.setString(7, row.status());
                    ps.setBoolean(8, row.newsletterOptIn());
                    setInstant(ps, 9, row.newsletterConsentAt());
                    setInstant(ps, 10, row.lastLoginAt());
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

    private static String sourcePattern(String prefix) {
        return "%\"source\":\"" + prefix + "\"%";
    }

    private static void setInstant(PreparedStatement ps, int index, Instant value)
            throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.from(value));
        }
    }
}
