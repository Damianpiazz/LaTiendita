package com.latiendita.productService.seeder;

import com.latiendita.productService.seeder.model.SeedDataset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Verificaciones reales sobre PostgreSQL antes de publicar el éxito del seeder. */
@Component
public class SeederValidator {

    private final SeederJdbcRepository repository;
    private final SeederProperties properties;

    public SeederValidator(SeederJdbcRepository repository, SeederProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public void validate(SeedDataset dataset, String version) {
        String prefix = properties.getExternalIdPrefix() + "-";
        List<String> errors = new ArrayList<>();

        check(errors, "categorías sintéticas",
                repository.countWhere("SELECT count(*) FROM categories WHERE name LIKE ?",
                        prefix + "CAT-%"), dataset.categories().size());
        check(errors, "productos sintéticos",
                repository.countWhere("SELECT count(*) FROM products WHERE sku LIKE ?",
                        prefix + "SKU-%"), dataset.products().size());
        check(errors, "SKU únicos",
                repository.countWhere("SELECT count(DISTINCT sku) FROM products WHERE sku LIKE ?",
                        prefix + "SKU-%"), dataset.products().size());
        check(errors, "slugs únicos",
                repository.countWhere("SELECT count(DISTINCT slug) FROM products WHERE slug LIKE ?",
                        "producto-" + properties.getExternalIdPrefix().toLowerCase() + "-%"),
                dataset.products().size());

        long orphans = repository.countWhere("""
                SELECT count(*) FROM products p
                LEFT JOIN categories c ON c.id = p.category_id
                WHERE p.sku LIKE ? AND c.id IS NULL
                """, prefix + "SKU-%");
        if (orphans != 0) {
            errors.add("productos sin categoría padre: " + orphans);
        }

        long unpublished = repository.countWhere(
                "SELECT count(*) FROM products WHERE sku LIKE ? AND published = false",
                prefix + "SKU-%");
        if (unpublished != 0) {
            errors.add("productos no publicados: " + unpublished);
        }

        long invalidJson = repository.countWhere("""
                SELECT count(*) FROM products
                WHERE sku LIKE ? AND (images_json IS NULL OR specs_json IS NULL
                                      OR images_json NOT LIKE '[%'
                                      OR specs_json NOT LIKE '[%')
                """, prefix + "SKU-%");
        if (invalidJson != 0) {
            errors.add("productos con JSON inválido: " + invalidJson);
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Validación del seeder " + version + " fallida: "
                    + String.join("; ", errors));
        }
    }

    public void validateMarker(String version) {
        if (properties.isMarkerEnabled() && !repository.verifyMarker(version)) {
            throw new IllegalStateException("No se pudo verificar el marcador del seeder "
                    + version);
        }
    }

    private static void check(List<String> errors, String label, long actual, long expected) {
        if (actual != expected) {
            errors.add(label + ": esperado " + expected + ", encontrado " + actual);
        }
    }
}
