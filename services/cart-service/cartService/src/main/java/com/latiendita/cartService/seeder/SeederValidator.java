package com.latiendita.cartService.seeder;

import com.latiendita.cartService.seeder.model.SeedDataset;
import com.latiendita.cartService.seeder.model.SeededCartItem;
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
        String titlePattern = "Producto sintético " + properties.getExternalIdPrefix()
                + "-SKU-%";
        String cartScope = "FROM carts c WHERE c.id IN "
                + "(SELECT cart_id FROM cart_items WHERE product_title LIKE ?)";
        String itemScope = "FROM cart_items WHERE product_title LIKE ?";
        List<String> errors = new ArrayList<>();

        check(errors, "carritos sintéticos",
                repository.countWhere("SELECT count(*) " + cartScope, titlePattern),
                dataset.carts().size());
        check(errors, "usuarios únicos",
                repository.countWhere("SELECT count(DISTINCT c.user_id) " + cartScope,
                        titlePattern), dataset.carts().size());
        check(errors, "carritos con versión inválida",
                repository.countWhere("SELECT count(*) " + cartScope + " AND c.version <> 0",
                        titlePattern), 0L);
        check(errors, "carritos sin actualización posterior",
                repository.countWhere("SELECT count(*) " + cartScope
                        + " AND c.updated_at <= c.created_at", titlePattern), 0L);

        check(errors, "ítems sintéticos",
                repository.countWhere("SELECT count(*) " + itemScope, titlePattern),
                dataset.items().size());
        check(errors, "ítems huérfanos",
                repository.countWhere("SELECT count(*) " + itemScope
                        + " AND cart_id NOT IN (SELECT id FROM carts)", titlePattern), 0L);
        check(errors, "productos repetidos por carrito",
                repository.countWhere("SELECT count(*) FROM (SELECT cart_id, product_id "
                        + itemScope + " GROUP BY cart_id, product_id HAVING count(*) > 1) x",
                        titlePattern), 0L);
        check(errors, "cantidades inválidas",
                repository.countWhere("SELECT count(*) " + itemScope
                        + " AND (quantity < 1 OR quantity > 99)", titlePattern), 0L);
        check(errors, "precios inválidos",
                repository.countWhere("SELECT count(*) " + itemScope + " AND unit_price <= 0",
                        titlePattern), 0L);
        check(errors, "ítems sin fecha de actualización",
                repository.countWhere("SELECT count(*) " + itemScope
                        + " AND updated_at <= created_at", titlePattern), 0L);
        long expectedPriceChanged = dataset.items().stream()
                .filter(SeededCartItem::priceChanged).count();
        check(errors, "ítems con precio modificado",
                repository.countWhere("SELECT count(*) " + itemScope
                        + " AND price_changed", titlePattern), expectedPriceChanged);

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
