package com.latiendita.reviewService.seeder;

import com.latiendita.reviewService.seeder.model.SeedDataset;
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
        long base = properties.getExternalIdBase();
        int count = properties.getCount();
        long productStart = base + SeederJdbcRepository.PRODUCT_OFFSET;
        long productEnd = productStart + count - 1;
        long orderStart = base + SeederJdbcRepository.ORDER_OFFSET;
        long orderEnd = orderStart + count - 1;
        String eventPattern = SeederJdbcRepository.EVENT_PATTERN;
        List<String> errors = new ArrayList<>();

        check(errors, "reseñas",
                repository.countWhere("SELECT count(*) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.reviews().size());
        check(errors, "ids de reseña únicos",
                repository.countWhere("SELECT count(DISTINCT id) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.reviews().size());
        check(errors, "pares cliente-producto únicos",
                repository.countWhere("SELECT count(DISTINCT customer_id || ':' || product_id) "
                        + "FROM reviews WHERE order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.reviews().size());
        check(errors, "ratings fuera de rango",
                repository.countWhere("SELECT count(*) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ? AND (rating < 1 OR rating > 5)",
                        orderStart, orderEnd), 0L);
        check(errors, "estados inválidos",
                repository.countWhere("SELECT count(*) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ? "
                        + "AND status NOT IN ('approved','pending','rejected')",
                        orderStart, orderEnd), 0L);
        check(errors, "rechazadas sin motivo",
                repository.countWhere("SELECT count(*) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ? AND status='rejected' "
                        + "AND (moderation_reason IS NULL OR btrim(moderation_reason)='')",
                        orderStart, orderEnd), 0L);
        check(errors, "no rechazadas con motivo",
                repository.countWhere("SELECT count(*) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ? AND status<>'rejected' "
                        + "AND moderation_reason IS NOT NULL", orderStart, orderEnd), 0L);
        check(errors, "productos fuera de rango",
                repository.countWhere("SELECT count(*) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ? "
                        + "AND (product_id < ? OR product_id >= ?)",
                        orderStart, orderEnd, productStart, productEnd), 0L);
        check(errors, "helpful_votes incoherente",
                repository.countWhere("SELECT count(*) FROM reviews r "
                        + "WHERE r.order_id BETWEEN ? AND ? "
                        + "AND r.helpful_votes <> (SELECT count(*) FROM review_votes v "
                        + "WHERE v.review_id=r.id)", orderStart, orderEnd), 0L);
        check(errors, "total de votos útiles",
                repository.countWhere("SELECT coalesce(sum(helpful_votes),0) FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.votes().size());

        check(errors, "votos útiles",
                repository.countWhere("SELECT count(*) FROM review_votes v "
                        + "JOIN reviews r ON r.id=v.review_id "
                        + "WHERE r.order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.votes().size());
        check(errors, "ids de voto únicos",
                repository.countWhere("SELECT count(DISTINCT v.id) FROM review_votes v "
                        + "JOIN reviews r ON r.id=v.review_id "
                        + "WHERE r.order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.votes().size());
        check(errors, "votantes únicos",
                repository.countWhere("SELECT count(DISTINCT v.customer_id) FROM review_votes v "
                        + "JOIN reviews r ON r.id=v.review_id "
                        + "WHERE r.order_id BETWEEN ? AND ?", orderStart, orderEnd),
                dataset.votes().size());
        check(errors, "pares reseña-votante duplicados",
                repository.countWhere("SELECT count(*) FROM (SELECT v.review_id, v.customer_id "
                        + "FROM review_votes v JOIN reviews r ON r.id=v.review_id "
                        + "WHERE r.order_id BETWEEN ? AND ? "
                        + "GROUP BY v.review_id, v.customer_id HAVING count(*) > 1) x",
                        orderStart, orderEnd), 0L);
        check(errors, "votos huérfanos",
                repository.countWhere("SELECT count(*) FROM review_votes v "
                        + "LEFT JOIN reviews r ON r.id=v.review_id WHERE r.id IS NULL"), 0L);
        check(errors, "votos propios",
                repository.countWhere("SELECT count(*) FROM review_votes v "
                        + "JOIN reviews r ON r.id=v.review_id "
                        + "WHERE r.order_id BETWEEN ? AND ? AND r.customer_id=v.customer_id",
                        orderStart, orderEnd), 0L);

        check(errors, "agregados de calificación",
                repository.countWhere("SELECT count(*) FROM product_ratings "
                        + "WHERE product_id BETWEEN ? AND ?", productStart, productEnd),
                dataset.ratings().size());
        check(errors, "productos de agregado únicos",
                repository.countWhere("SELECT count(DISTINCT product_id) FROM product_ratings "
                        + "WHERE product_id BETWEEN ? AND ?", productStart, productEnd),
                dataset.ratings().size());
        check(errors, "agregados huérfanos",
                repository.countWhere("SELECT count(*) FROM product_ratings pr "
                        + "WHERE pr.product_id BETWEEN ? AND ? AND NOT EXISTS ("
                        + "SELECT 1 FROM reviews r WHERE r.product_id=pr.product_id "
                        + "AND r.order_id BETWEEN ? AND ? AND r.status='approved')",
                        productStart, productEnd, orderStart, orderEnd), 0L);
        check(errors, "agregados faltantes",
                repository.countWhere("SELECT count(*) FROM (SELECT DISTINCT product_id "
                        + "FROM reviews WHERE order_id BETWEEN ? AND ? AND status='approved') a "
                        + "WHERE NOT EXISTS (SELECT 1 FROM product_ratings pr "
                        + "WHERE pr.product_id=a.product_id)", orderStart, orderEnd), 0L);
        check(errors, "review_count de agregado inválido",
                repository.countWhere("SELECT count(*) FROM product_ratings pr "
                        + "LEFT JOIN (SELECT product_id, count(*) AS c FROM reviews "
                        + "WHERE order_id BETWEEN ? AND ? AND status='approved' "
                        + "GROUP BY product_id) a ON a.product_id=pr.product_id "
                        + "WHERE pr.product_id BETWEEN ? AND ? "
                        + "AND (a.c IS NULL OR pr.review_count <> a.c)",
                        orderStart, orderEnd, productStart, productEnd), 0L);
        check(errors, "average de agregado inválido",
                repository.countWhere("SELECT count(*) FROM product_ratings pr "
                        + "LEFT JOIN (SELECT product_id, round(avg(rating),2) AS avg_rating "
                        + "FROM reviews WHERE order_id BETWEEN ? AND ? AND status='approved' "
                        + "GROUP BY product_id) a ON a.product_id=pr.product_id "
                        + "WHERE pr.product_id BETWEEN ? AND ? "
                        + "AND (a.avg_rating IS NULL OR pr.average <> a.avg_rating)",
                        orderStart, orderEnd, productStart, productEnd), 0L);
        check(errors, "distribución sin claves 1..5",
                repository.countWhere("SELECT count(*) FROM product_ratings "
                        + "WHERE product_id BETWEEN ? AND ? AND ("
                        + "distribution->>'1' IS NULL OR distribution->>'2' IS NULL"
                        + " OR distribution->>'3' IS NULL OR distribution->>'4' IS NULL"
                        + " OR distribution->>'5' IS NULL)", productStart, productEnd), 0L);
        check(errors, "suma de distribución inválida",
                repository.countWhere("SELECT count(*) FROM product_ratings "
                        + "WHERE product_id BETWEEN ? AND ? AND ("
                        + "(distribution->>'1')::int + (distribution->>'2')::int"
                        + " + (distribution->>'3')::int + (distribution->>'4')::int"
                        + " + (distribution->>'5')::int) <> review_count",
                        productStart, productEnd), 0L);
        for (int star = 1; star <= 5; star++) {
            check(errors, "distribución de " + star + " estrellas inválida",
                    repository.countWhere("SELECT count(*) FROM product_ratings pr "
                            + "LEFT JOIN (SELECT product_id, count(*) AS c FROM reviews "
                            + "WHERE order_id BETWEEN ? AND ? AND status='approved' "
                            + "AND rating=? GROUP BY product_id) a ON a.product_id=pr.product_id "
                            + "WHERE pr.product_id BETWEEN ? AND ? "
                            + "AND (pr.distribution->>?)::int <> coalesce(a.c,0)",
                            orderStart, orderEnd, star, productStart, productEnd,
                            String.valueOf(star)), 0L);
        }

        if (properties.isIncludeInfra()) {
            check(errors, "eventos outbox",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ?", eventPattern), dataset.events().size());
            check(errors, "eventos pendientes o fallidos",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ? AND status <> 'SENT'", eventPattern), 0L);
            check(errors, "eventos sin fecha de envío",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ? AND sent_at IS NULL", eventPattern), 0L);
            check(errors, "eventos con attempts inválidos",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ? AND attempts <> 1", eventPattern), 0L);
            check(errors, "topics inválidos",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ? AND topic <> 'review-events'", eventPattern),
                    0L);
            check(errors, "tipos de evento inválidos",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ? AND event_type <> 'REVIEW_APPROVED'",
                            eventPattern), 0L);
            check(errors, "eventos sin tipo en el payload",
                    repository.countWhere("SELECT count(*) FROM outbox_events "
                            + "WHERE payload LIKE ? AND payload NOT LIKE '%REVIEW_APPROVED%'",
                            eventPattern), 0L);
            check(errors, "agregados de evento únicos",
                    repository.countWhere("SELECT count(DISTINCT aggregate_id) "
                            + "FROM outbox_events WHERE payload LIKE ?", eventPattern),
                    dataset.events().size());
            check(errors, "eventos huérfanos",
                    repository.countWhere("SELECT count(*) FROM outbox_events e "
                            + "WHERE e.payload LIKE ? AND e.aggregate_id NOT IN ("
                            + "SELECT id::text FROM reviews WHERE order_id BETWEEN ? AND ? "
                            + "AND status='approved')", eventPattern, orderStart, orderEnd), 0L);
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
