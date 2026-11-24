package com.latiendita.cartService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.latiendita.cartService.seeder.model.SeedDataset;
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
        "seeder.external-id-base=9000000"
})
@ActiveProfiles("test")
class DatabaseSeederIntegrationTest {

    private static final String REAL_CART = "99999999-0000-0000-0000-000000000001";
    private static final String TITLE_PATTERN = "Producto sintético SEED-SKU-%";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseSeederService service;

    @Autowired
    private SeederProperties properties;

    @Autowired
    private SeederValidator validator;

    @Autowired
    private CartDataGenerator generator;

    @Test
    void shouldSeedVerifyStayIdempotentAndCleanOnlySyntheticRows() {
        insertRealCart();
        int expectedItems = generator.generate().items().size();

        SeedExecution first = service.seed();
        assertThat(first.executed()).isTrue();
        assertThat(first.carts()).isEqualTo(30);
        assertThat(first.items()).isEqualTo(expectedItems);
        assertThat(count("SELECT count(*) FROM cart_items WHERE product_title LIKE ?",
                TITLE_PATTERN)).isEqualTo(expectedItems);
        assertThat(count("SELECT count(DISTINCT cart_id || ':' || product_id) FROM cart_items "
                + "WHERE product_title LIKE ?", TITLE_PATTERN)).isEqualTo(expectedItems);
        assertThat(count("SELECT count(*) FROM cart_items WHERE product_title LIKE ? "
                + "AND cart_id NOT IN (SELECT id FROM carts)", TITLE_PATTERN)).isZero();
        assertThat(count("SELECT count(*) FROM seeder_markers WHERE service = 'cart' "
                + "AND version = 'cart-v1'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM carts WHERE id = ?::uuid", REAL_CART)).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM carts")).isEqualTo(31);

        SeedExecution second = service.seed();
        assertThat(second.executed()).isFalse();
        assertThat(second.outcome()).isEqualTo("already_seeded");
        assertThat(count("SELECT count(*) FROM cart_items WHERE product_title LIKE ?",
                TITLE_PATTERN)).isEqualTo(expectedItems);

        SeedDataset dataset = generator.generate();
        jdbc.update("DELETE FROM cart_items WHERE id = ?", dataset.items().get(0).id());
        assertThatThrownBy(() -> validator.validate(dataset,
                DatabaseSeederService.VERSION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ítems sintéticos");

        jdbc.update("DELETE FROM seeder_markers WHERE service = 'cart'");
        properties.setCleanBeforeRun(true);
        properties.setAllowClean(true);
        SeedExecution cleaned = service.seed();

        assertThat(cleaned.executed()).isTrue();
        assertThat(count("SELECT count(*) FROM cart_items WHERE product_title LIKE ?",
                TITLE_PATTERN)).isEqualTo(expectedItems);
        assertThat(count("SELECT count(*) FROM carts WHERE id = ?::uuid", REAL_CART)).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM carts")).isEqualTo(31);
    }

    private void insertRealCart() {
        jdbc.update("""
                INSERT INTO carts (id, user_id, created_at, updated_at, version)
                VALUES (?, 'real-user', now(), now(), 0)
                """, java.util.UUID.fromString(REAL_CART));
        jdbc.update("""
                INSERT INTO cart_items
                  (id, cart_id, product_id, quantity, unit_price, product_title,
                   price_changed, created_at, updated_at)
                VALUES (?, ?, 123, 1, 10.00, 'Producto real', false, now(), now())
                """, java.util.UUID.fromString("99999999-0000-0000-0000-000000000002"),
                java.util.UUID.fromString(REAL_CART));
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }
}
