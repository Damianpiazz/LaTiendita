package com.latiendita.productService.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.productService.TestcontainersConfiguration;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The repo manages its schema with Hibernate `ddl-auto=update`. This test starts
 * from a legacy table containing a row, so it fails if the new column is added
 * without a database default or if existing data is lost.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-legacy-categories.sql"
})
class CategoryFeaturedMigrationIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldKeepLegacyRowsWithFeaturedFalse() {
        CategoryEntity legacy = categoryRepository.findByName("Legacy Category").orElseThrow();

        assertThat(legacy.isFeatured()).isFalse();
        assertThat(legacy.getDescription()).isEqualTo("Creada antes de featured");
    }

    @Test
    void shouldExposeFeaturedColumnWithDatabaseDefaultFalse() {
        String defaultExpression = jdbcTemplate.queryForObject("""
                select column_default from information_schema.columns
                where table_name = 'categories' and column_name = 'featured'
                """, String.class);
        String nullable = jdbcTemplate.queryForObject("""
                select is_nullable from information_schema.columns
                where table_name = 'categories' and column_name = 'featured'
                """, String.class);

        assertThat(defaultExpression).isNotNull();
        assertThat(defaultExpression.toLowerCase(Locale.ROOT)).contains("false");
        assertThat(nullable).isEqualTo("NO");
    }
}
