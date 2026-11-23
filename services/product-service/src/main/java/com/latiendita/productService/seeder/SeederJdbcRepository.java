package com.latiendita.productService.seeder;

import com.latiendita.productService.seeder.model.SeedCategory;
import com.latiendita.productService.seeder.model.SeedProduct;
import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Acceso JDBC directo: el seeder no pasa por JPA ni por los repositorios de
 * negocio. Así puede insertar IDs asignados, usar `ON CONFLICT DO NOTHING` y
 * agrupar filas en batches sin interactuar con el ciclo de vida de Hibernate.
 */
@Repository
public class SeederJdbcRepository {

    static final String SERVICE = "product";

    private static final String INSERT_CATEGORY = """
            INSERT INTO categories (id, name, description, slug, active, featured, image_url, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_PRODUCT = """
            INSERT INTO products
              (id, title, image_url, sku, price, quantity, category_id, slug, subcategory,
               featured, published, original_price, fulfillment, images_json,
               description_json, highlights_json, specs_json, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
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
        String prefix = properties.getExternalIdPrefix() + "-";
        transaction.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM products WHERE sku LIKE ?", prefix + "SKU-%");
            jdbc.update("DELETE FROM categories WHERE name LIKE ?", prefix + "CAT-%");
            deleteMarkers();
        });
    }

    public int insertCategories(List<SeedCategory> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_CATEGORY, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeedCategory row) -> {
                    ps.setLong(1, row.id());
                    ps.setString(2, row.name());
                    ps.setString(3, row.description());
                    ps.setString(4, row.slug());
                    ps.setBoolean(5, row.active());
                    ps.setBoolean(6, row.featured());
                    ps.setString(7, row.imageUrl());
                });
        return rows.size();
    }

    public int insertProducts(List<SeedProduct> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT_PRODUCT, rows, properties.getBatchSize(),
                (PreparedStatement ps, SeedProduct row) -> {
                    ps.setLong(1, row.id());
                    ps.setString(2, row.title());
                    ps.setString(3, row.imageUrl());
                    ps.setString(4, row.sku());
                    ps.setBigDecimal(5, row.price());
                    ps.setInt(6, row.quantity());
                    ps.setLong(7, row.categoryId());
                    ps.setString(8, row.slug());
                    ps.setString(9, row.subcategory());
                    ps.setBoolean(10, row.featured());
                    ps.setBoolean(11, row.published());
                    ps.setBigDecimal(12, row.originalPrice());
                    ps.setString(13, row.fulfillment());
                    ps.setString(14, row.imagesJson());
                    ps.setString(15, row.descriptionJson());
                    ps.setString(16, row.highlightsJson());
                    ps.setString(17, row.specsJson());
                });
        return rows.size();
    }

    /** Ajusta la secuencia de identidad para que el próximo ID no choque con los sintéticos. */
    public void resetIdentity(String table) {
        Long max = jdbc.queryForObject("SELECT max(id) FROM " + table, Long.class);
        if (max == null) {
            jdbc.execute("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), 1, false)");
        } else {
            jdbc.execute("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), "
                    + max + ", true)");
        }
    }
}
