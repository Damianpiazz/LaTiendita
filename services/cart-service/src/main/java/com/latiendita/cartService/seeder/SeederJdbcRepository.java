package com.latiendita.cartService.seeder;

import com.latiendita.cartService.seeder.model.SeededCart;
import com.latiendita.cartService.seeder.model.SeededCartItem;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** Acceso JDBC directo: inserciones por batch y marcador sintético. */
@Repository
public class SeederJdbcRepository {

    static final String SERVICE = "cart";

    private static final String INSERT_CART = """
            INSERT INTO carts (id, user_id, created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_ITEM = """
            INSERT INTO cart_items
              (id, cart_id, product_id, quantity, unit_price, product_title,
               image_url, price_changed, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
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

    /** Limpieza controlada: borra sólo carritos e ítems sintéticos y el marcador. */
    public void cleanSeededRows(List<SeededCart> carts) {
        String titlePattern = "Producto sintético " + properties.getExternalIdPrefix()
                + "-SKU-%";
        transaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM cart_items WHERE product_title LIKE ?", titlePattern);
            List<UUID> ids = carts.stream().map(SeededCart::id).toList();
            jdbc.batchUpdate("DELETE FROM cart_items WHERE cart_id = ?", ids,
                    properties.getBatchSize(),
                    (PreparedStatement ps, UUID id) -> ps.setObject(1, id));
            jdbc.batchUpdate("DELETE FROM carts WHERE id = ?", ids,
                    properties.getBatchSize(),
                    (PreparedStatement ps, UUID id) -> ps.setObject(1, id));
            deleteMarkers();
        });
    }

    public int insertCarts(List<SeededCart> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_CART, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededCart row) -> {
                    ps.setObject(1, row.id());
                    ps.setString(2, row.userId());
                    setInstant(ps, 3, row.createdAt());
                    setInstant(ps, 4, row.updatedAt());
                    ps.setLong(5, row.version());
                });
        return rows.size();
    }

    public int insertItems(List<SeededCartItem> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_ITEM, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeededCartItem row) -> {
                    ps.setObject(1, row.id());
                    ps.setObject(2, row.cartId());
                    ps.setLong(3, row.productId());
                    ps.setInt(4, row.quantity());
                    ps.setBigDecimal(5, row.unitPrice());
                    ps.setString(6, row.productTitle());
                    ps.setString(7, row.imageUrl());
                    ps.setBoolean(8, row.priceChanged());
                    setInstant(ps, 9, row.createdAt());
                    setInstant(ps, 10, row.updatedAt());
                });
        return rows.size();
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
