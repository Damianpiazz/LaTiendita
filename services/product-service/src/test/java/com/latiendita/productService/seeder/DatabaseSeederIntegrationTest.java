package com.latiendita.productService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.productService.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Ejecuta el seeder real contra PostgreSQL y comprueba idempotencia e integridad. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "seeder.enabled=true",
        "seeder.count=30",
        "seeder.batch-size=10",
        "seeder.random-seed=99",
        "seeder.external-id-prefix=SEED",
        "seeder.external-id-base=9000000"
})
@ActiveProfiles("test")
class DatabaseSeederIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseSeederService service;

    @Test
    void shouldSeedExpectedVolumeAndRelations() {
        assertThat(count("SELECT count(*) FROM categories WHERE name LIKE 'SEED-CAT-%'"))
                .isEqualTo(20);
        assertThat(count("SELECT count(*) FROM products WHERE sku LIKE 'SEED-SKU-%'"))
                .isEqualTo(30);
        assertThat(count("SELECT count(DISTINCT sku) FROM products WHERE sku LIKE 'SEED-SKU-%'"))
                .isEqualTo(30);
        assertThat(count("SELECT count(DISTINCT slug) FROM products WHERE slug LIKE "
                + "'producto-seed-%'")).isEqualTo(30);
        assertThat(count("""
                SELECT count(*) FROM products p
                LEFT JOIN categories c ON c.id = p.category_id
                WHERE p.sku LIKE 'SEED-SKU-%' AND c.id IS NULL
                """)).isZero();
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'product' "
                + "AND version = 'product-v1'")).isEqualTo(1);
        assertThat(count("""
                SELECT last_value FROM pg_sequences
                WHERE schemaname = 'public' AND sequencename = 'categories_id_seq'
                """)).isGreaterThanOrEqualTo(9_000_020L);
    }

    @Test
    void shouldBeIdempotentWhenRunAgain() {
        SeedExecution second = service.run();

        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM products WHERE sku LIKE 'SEED-SKU-%'"))
                .isEqualTo(30);
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'product'"))
                .isEqualTo(1);
    }

    private long count(String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
