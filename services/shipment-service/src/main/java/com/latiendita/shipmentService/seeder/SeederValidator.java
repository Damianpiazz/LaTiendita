package com.latiendita.shipmentService.seeder;

import com.latiendita.shipmentService.seeder.model.SeedDataset;
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
        String customerPattern = properties.getExternalIdPrefix() + "-CUST-%";
        List<String> errors = new ArrayList<>();

        check(errors, "envíos sintéticos",
                repository.countWhere("SELECT count(*) FROM shipments WHERE customer_id LIKE ?",
                        customerPattern), dataset.shipments().size());
        check(errors, "order_id únicos",
                repository.countWhere(
                        "SELECT count(DISTINCT order_id) FROM shipments WHERE customer_id LIKE ?",
                        customerPattern), dataset.shipments().size());
        check(errors, "direcciones sintéticas",
                repository.countWhere(
                        "SELECT count(*) FROM delivery_addresses WHERE customer_id LIKE ?",
                        customerPattern), dataset.addresses().size());
        check(errors, "direcciones por cliente",
                repository.countWhere(
                        "SELECT count(DISTINCT customer_id) FROM delivery_addresses "
                                + "WHERE customer_id LIKE ?",
                        customerPattern), dataset.addresses().size());
        check(errors, "direcciones no predeterminadas",
                repository.countWhere(
                        "SELECT count(*) FROM delivery_addresses WHERE customer_id LIKE ? "
                                + "AND is_default = false",
                        customerPattern), 0L);
        check(errors, "estados inválidos",
                repository.countWhere("""
                        SELECT count(*) FROM shipments WHERE customer_id LIKE ?
                        AND status NOT IN ('AWAITING_PAYMENT', 'PENDING_DISPATCH',
                                           'DISPATCHED', 'DELIVERED', 'CANCELLED')
                        """, customerPattern), 0L);
        check(errors, "rangos de fecha inválidos",
                repository.countWhere("""
                        SELECT count(*) FROM shipments WHERE customer_id LIKE ?
                        AND estimated_to <= estimated_from
                        """, customerPattern), 0L);
        check(errors, "envíos sin importe",
                repository.countWhere("""
                        SELECT count(*) FROM shipments WHERE customer_id LIKE ?
                        AND (shipping_cost IS NULL OR shipping_cost <= 0)
                        """, customerPattern), 0L);

        String eventJoin = """
                FROM outbox_events e
                JOIN shipments s ON CAST(s.order_id AS varchar(64)) = e.aggregate_id
                WHERE s.customer_id LIKE ? AND e.topic = 'shipment-events'
                """;
        check(errors, "eventos de infraestructura",
                repository.countWhere("SELECT count(*) " + eventJoin, customerPattern),
                dataset.events().size());
        check(errors, "eventos no enviados",
                repository.countWhere(
                        "SELECT count(*) " + eventJoin + " AND e.status <> 'SENT'",
                        customerPattern), 0L);
        check(errors, "eventos sin sent_at",
                repository.countWhere(
                        "SELECT count(*) " + eventJoin + " AND e.sent_at IS NULL",
                        customerPattern), 0L);

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
