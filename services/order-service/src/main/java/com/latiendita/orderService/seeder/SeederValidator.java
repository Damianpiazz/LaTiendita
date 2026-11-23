package com.latiendita.orderService.seeder;

import com.latiendita.orderService.seeder.model.SeedDataset;
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
        String orderPattern = properties.getExternalIdPrefix() + "-ORD-%";
        List<String> errors = new ArrayList<>();

        check(errors, "pedidos sintéticos",
                repository.countWhere("SELECT count(*) FROM orders WHERE order_number LIKE ?",
                        orderPattern), dataset.orders().size());
        check(errors, "números de pedido únicos",
                repository.countWhere(
                        "SELECT count(DISTINCT order_number) FROM orders WHERE order_number LIKE ?",
                        orderPattern), dataset.orders().size());
        check(errors, "ítems sintéticos",
                repository.countWhere("""
                        SELECT count(*) FROM order_items i
                        JOIN orders o ON o.id = i.order_id
                        WHERE o.order_number LIKE ?
                        """, orderPattern), dataset.items().size());
        check(errors, "ítems huérfanos",
                repository.countWhere("""
                        SELECT count(*) FROM order_items i
                        LEFT JOIN orders o ON o.id = i.order_id
                        WHERE o.id IS NULL
                        """), 0L);
        check(errors, "productos repetidos en un pedido",
                repository.countWhere("""
                        SELECT count(*) FROM (
                            SELECT o.id, i.product_id
                            FROM orders o JOIN order_items i ON i.order_id = o.id
                            WHERE o.order_number LIKE ?
                            GROUP BY o.id, i.product_id
                            HAVING count(*) > 1
                        ) duplicates
                        """, orderPattern), 0L);
        check(errors, "totales incorrectos",
                repository.countWhere("""
                        SELECT count(*) FROM (
                            SELECT o.id
                            FROM orders o JOIN order_items i ON i.order_id = o.id
                            WHERE o.order_number LIKE ?
                            GROUP BY o.id, o.total
                            HAVING o.total <> SUM(i.subtotal)
                        ) mismatches
                        """, orderPattern), 0L);
        check(errors, "cantidades inválidas",
                repository.countWhere("""
                        SELECT count(*) FROM order_items i
                        JOIN orders o ON o.id = i.order_id
                        WHERE o.order_number LIKE ? AND i.quantity < 1
                        """, orderPattern), 0L);
        check(errors, "estados inválidos",
                repository.countWhere("""
                        SELECT count(*) FROM orders WHERE order_number LIKE ?
                        AND status NOT IN ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')
                        """, orderPattern), 0L);
        check(errors, "cancelaciones sin motivo",
                repository.countWhere("""
                        SELECT count(*) FROM orders WHERE order_number LIKE ?
                        AND status = 'CANCELLED' AND (cancel_reason IS NULL OR cancel_reason = '')
                        """, orderPattern), 0L);
        check(errors, "pedidos no cancelados con motivo",
                repository.countWhere("""
                        SELECT count(*) FROM orders WHERE order_number LIKE ?
                        AND status <> 'CANCELLED' AND cancel_reason IS NOT NULL
                        """, orderPattern), 0L);
        check(errors, "reembolsos en pedidos no cancelados",
                repository.countWhere("""
                        SELECT count(*) FROM orders WHERE order_number LIKE ?
                        AND status <> 'CANCELLED' AND refund_required = true
                        """, orderPattern), 0L);

        String eventJoin = """
                FROM outbox_events e
                JOIN orders o ON CAST(o.id AS varchar(64)) = e.aggregate_id
                WHERE o.order_number LIKE ? AND e.topic = 'order-events'
                """;
        check(errors, "eventos de infraestructura",
                repository.countWhere("SELECT count(*) " + eventJoin, orderPattern),
                dataset.events().size());
        check(errors, "eventos ORDER_CREATED",
                repository.countWhere(
                        "SELECT count(*) " + eventJoin + " AND e.event_type = 'ORDER_CREATED'",
                        orderPattern), dataset.orders().size());
        check(errors, "eventos no enviados",
                repository.countWhere("SELECT count(*) " + eventJoin + " AND e.status <> 'SENT'",
                        orderPattern), 0L);
        check(errors, "eventos sin sent_at",
                repository.countWhere("SELECT count(*) " + eventJoin + " AND e.sent_at IS NULL",
                        orderPattern), 0L);

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
