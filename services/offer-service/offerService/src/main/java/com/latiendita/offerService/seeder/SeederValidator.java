package com.latiendita.offerService.seeder;

import com.latiendita.offerService.seeder.model.SeedDataset;
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
        String prefix = properties.getExternalIdPrefix();
        String promoPattern = prefix + "-PROMO-%";
        String usagePattern = prefix + "-CUST-%";
        String benefitPattern = prefix + "-BENEFIT-%";
        String eventPattern = "%\"source\":\"SEED\"%";
        String promoScope = "FROM promotions WHERE code LIKE ?";
        String usageScope = "FROM coupon_usages WHERE customer_sub LIKE ?";
        String benefitScope = "FROM benefits WHERE code LIKE ?";
        String eventScope = "FROM outbox_events WHERE payload LIKE ?";
        List<String> errors = new ArrayList<>();

        check(errors, "promociones",
                repository.countWhere("SELECT count(*) " + promoScope, promoPattern),
                dataset.promotions().size());
        check(errors, "códigos de promoción únicos",
                repository.countWhere("SELECT count(DISTINCT code) " + promoScope, promoPattern),
                dataset.promotions().size());
        check(errors, "ids de promoción únicos",
                repository.countWhere("SELECT count(DISTINCT id) " + promoScope, promoPattern),
                dataset.promotions().size());
        check(errors, "tipos inválidos",
                repository.countWhere("SELECT count(*) " + promoScope
                        + " AND type NOT IN ('FEATURED','PERCENTAGE','FIXED_AMOUNT')",
                        promoPattern), 0L);
        check(errors, "scopes inválidos",
                repository.countWhere("SELECT count(*) " + promoScope
                        + " AND scope NOT IN ('PRODUCT','CATEGORY','ORDER')", promoPattern), 0L);
        check(errors, "scope sin objetivo coherente",
                repository.countWhere("SELECT count(*) " + promoScope + " AND ("
                        + "(scope='PRODUCT' AND product_id IS NULL)"
                        + " OR (scope='CATEGORY' AND category_id IS NULL)"
                        + " OR (scope='ORDER' AND (product_id IS NOT NULL"
                        + " OR category_id IS NOT NULL)))", promoPattern), 0L);
        check(errors, "FEATURED con descuento",
                repository.countWhere("SELECT count(*) " + promoScope
                        + " AND type='FEATURED' AND discount_value IS NOT NULL", promoPattern), 0L);
        check(errors, "descuentos inválidos",
                repository.countWhere("SELECT count(*) " + promoScope
                        + " AND type<>'FEATURED' AND (discount_value IS NULL OR discount_value<=0)",
                        promoPattern), 0L);
        check(errors, "ventanas temporales inválidas",
                repository.countWhere("SELECT count(*) " + promoScope
                        + " AND ends_at <= starts_at", promoPattern), 0L);
        check(errors, "prioridades inválidas",
                repository.countWhere("SELECT count(*) " + promoScope
                        + " AND (priority < 1 OR priority > 10)", promoPattern), 0L);

        check(errors, "usos de cupón",
                repository.countWhere("SELECT count(*) " + usageScope, usagePattern),
                dataset.usages().size());
        check(errors, "pedidos duplicados en usos",
                repository.countWhere("SELECT count(DISTINCT order_id) " + usageScope,
                        usagePattern), dataset.usages().size());
        check(errors, "pares promoción-pedido duplicados",
                repository.countWhere("SELECT count(*) FROM (SELECT promotion_id, order_id "
                        + usageScope + " GROUP BY promotion_id, order_id HAVING count(*) > 1) x",
                        usagePattern), 0L);
        check(errors, "usos huérfanos",
                repository.countWhere("SELECT count(*) " + usageScope
                        + " AND promotion_id NOT IN (SELECT id FROM promotions)", usagePattern), 0L);
        check(errors, "usos sobre promociones FEATURED",
                repository.countWhere("SELECT count(*) FROM coupon_usages u"
                        + " JOIN promotions p ON p.id=u.promotion_id"
                        + " WHERE u.customer_sub LIKE ? AND p.type='FEATURED'",
                        usagePattern), 0L);
        check(errors, "pedidos fuera de rango",
                repository.countWhere("SELECT count(*) " + usageScope
                        + " AND (order_id < 9200000 OR order_id >= 9200000 + ?)",
                        usagePattern, dataset.usages().size()), 0L);

        check(errors, "beneficios",
                repository.countWhere("SELECT count(*) " + benefitScope, benefitPattern),
                dataset.benefits().size());
        check(errors, "códigos de beneficio únicos",
                repository.countWhere("SELECT count(DISTINCT code) " + benefitScope,
                        benefitPattern), dataset.benefits().size());
        check(errors, "beneficios inactivos",
                repository.countWhere("SELECT count(*) " + benefitScope + " AND NOT active",
                        benefitPattern), 0L);
        check(errors, "órdenes de beneficio inválidos",
                repository.countWhere("SELECT count(*) " + benefitScope
                        + " AND (sort_order < 1 OR sort_order > 20)", benefitPattern), 0L);

        if (properties.isIncludeInfra()) {
            check(errors, "eventos outbox",
                    repository.countWhere("SELECT count(*) " + eventScope, eventPattern),
                    dataset.events().size());
            check(errors, "eventos pendientes o fallidos",
                    repository.countWhere("SELECT count(*) " + eventScope
                            + " AND status <> 'SENT'", eventPattern), 0L);
            check(errors, "eventos sin fecha de envío",
                    repository.countWhere("SELECT count(*) " + eventScope
                            + " AND sent_at IS NULL", eventPattern), 0L);
            check(errors, "eventos con attempts inválidos",
                    repository.countWhere("SELECT count(*) " + eventScope
                            + " AND attempts <> 1", eventPattern), 0L);
            check(errors, "topics inválidos",
                    repository.countWhere("SELECT count(*) " + eventScope
                            + " AND topic <> 'promotion-events'", eventPattern), 0L);
            check(errors, "tipos de evento inválidos",
                    repository.countWhere("SELECT count(*) " + eventScope
                            + " AND event_type <> 'PROMOTION_CHANGED'", eventPattern), 0L);
            check(errors, "agregados únicos",
                    repository.countWhere("SELECT count(DISTINCT aggregate_id) " + eventScope,
                            eventPattern), dataset.events().size());
            check(errors, "eventos huérfanos",
                    repository.countWhere("SELECT count(*) FROM outbox_events e"
                            + " WHERE e.payload LIKE ?"
                            + " AND e.aggregate_id NOT IN (SELECT id::text FROM promotions)",
                            eventPattern), 0L);
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
