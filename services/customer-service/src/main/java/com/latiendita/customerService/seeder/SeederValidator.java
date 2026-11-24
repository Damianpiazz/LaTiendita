package com.latiendita.customerService.seeder;

import com.latiendita.customerService.seeder.model.SeedDataset;
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
        String subPattern = prefix + "-SUB-%";
        String emailPattern = prefix.toLowerCase(java.util.Locale.ROOT) + "+%@example.test";
        List<String> errors = new ArrayList<>();

        check(errors, "clientes sintéticos",
                repository.countWhere("SELECT count(*) FROM customers WHERE authorizer_sub LIKE ?",
                        subPattern), dataset.customers().size());
        check(errors, "authorizer_sub únicos",
                repository.countWhere(
                        "SELECT count(DISTINCT authorizer_sub) FROM customers WHERE authorizer_sub LIKE ?",
                        subPattern), dataset.customers().size());
        check(errors, "emails únicos",
                repository.countWhere(
                        "SELECT count(DISTINCT email) FROM customers WHERE email LIKE ?",
                        emailPattern), dataset.customers().size());

        check(errors, "clientes borrados lógicamente",
                repository.countWhere(
                        "SELECT count(*) FROM customers WHERE authorizer_sub LIKE ? "
                                + "AND deleted_at IS NOT NULL", subPattern), 0L);
        check(errors, "estados inválidos",
                repository.countWhere(
                        "SELECT count(*) FROM customers WHERE authorizer_sub LIKE ? "
                                + "AND status NOT IN ('ACTIVE', 'SUSPENDED')", subPattern), 0L);
        check(errors, "newsletter sin consentimiento",
                repository.countWhere(
                        "SELECT count(*) FROM customers WHERE authorizer_sub LIKE ? "
                                + "AND newsletter_opt_in = true AND newsletter_consent_at IS NULL",
                        subPattern), 0L);

        String eventJoin = """
                FROM outbox_events e
                JOIN customers c ON CAST(c.id AS varchar(64)) = e.aggregate_id
                WHERE c.authorizer_sub LIKE ? AND e.topic = 'customer-events'
                  AND e.event_type = 'CUSTOMER_CREATED'
                """;
        check(errors, "eventos de infraestructura",
                repository.countWhere("SELECT count(*) " + eventJoin, subPattern),
                dataset.events().size());
        check(errors, "eventos no enviados",
                repository.countWhere("SELECT count(*) " + eventJoin + " AND e.status <> 'SENT'",
                        subPattern), 0L);
        check(errors, "eventos sin sent_at",
                repository.countWhere("SELECT count(*) " + eventJoin + " AND e.sent_at IS NULL",
                        subPattern), 0L);

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
