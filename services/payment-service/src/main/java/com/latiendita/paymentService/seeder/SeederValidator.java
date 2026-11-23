package com.latiendita.paymentService.seeder;

import com.latiendita.paymentService.seeder.model.SeedDataset;
import com.latiendita.paymentService.seeder.model.SeededPayment;
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
        String idemPattern = prefix + "-IDEM-%";
        String notifPattern = prefix + "-NOTIF-%";
        String sourcePattern = "%\"source\":\"" + prefix + "\"%";
        List<String> errors = new ArrayList<>();

        check(errors, "pagos sintéticos",
                repository.countWhere("SELECT count(*) FROM payments WHERE idempotency_key LIKE ?",
                        idemPattern), dataset.payments().size());
        check(errors, "claves de idempotencia únicas",
                repository.countWhere(
                        "SELECT count(DISTINCT idempotency_key) FROM payments "
                                + "WHERE idempotency_key LIKE ?", idemPattern),
                dataset.payments().size());
        check(errors, "ids de proveedor únicos",
                repository.countWhere(
                        "SELECT count(DISTINCT provider_payment_id) FROM payments "
                                + "WHERE idempotency_key LIKE ?", idemPattern),
                dataset.payments().size());
        check(errors, "pedidos únicos",
                repository.countWhere(
                        "SELECT count(DISTINCT order_id) FROM payments WHERE idempotency_key LIKE ?",
                        idemPattern), dataset.payments().size());
        check(errors, "estados inválidos",
                repository.countWhere("""
                        SELECT count(*) FROM payments WHERE idempotency_key LIKE ?
                        AND status NOT IN ('PENDING','REJECTED','CANCELLED','EXPIRED','APPROVED')
                        """, idemPattern), 0L);
        check(errors, "proveedores inválidos",
                repository.countWhere("""
                        SELECT count(*) FROM payments WHERE idempotency_key LIKE ?
                        AND provider <> 'MERCADO_PAGO'
                        """, idemPattern), 0L);
        check(errors, "montos inválidos",
                repository.countWhere(
                        "SELECT count(*) FROM payments WHERE idempotency_key LIKE ? AND amount <= 0",
                        idemPattern), 0L);
        check(errors, "aprobados sin fecha de pago",
                repository.countWhere("""
                        SELECT count(*) FROM payments WHERE idempotency_key LIKE ?
                        AND status = 'APPROVED' AND paid_at IS NULL
                        """, idemPattern), 0L);
        check(errors, "no aprobados con fecha de pago",
                repository.countWhere("""
                        SELECT count(*) FROM payments WHERE idempotency_key LIKE ?
                        AND status <> 'APPROVED' AND paid_at IS NOT NULL
                        """, idemPattern), 0L);
        long expectedApproved = dataset.payments().stream()
                .filter(p -> "APPROVED".equals(p.status())).count();
        check(errors, "aprobados",
                repository.countWhere("""
                        SELECT count(*) FROM payments WHERE idempotency_key LIKE ?
                        AND status = 'APPROVED'
                        """, idemPattern), expectedApproved);

        String inbox = "FROM payment_events WHERE notification_id LIKE ?";
        check(errors, "notificaciones sintéticas",
                repository.countWhere("SELECT count(*) " + inbox, notifPattern),
                dataset.events().size());
        check(errors, "notificaciones procesadas",
                repository.countWhere("SELECT count(*) " + inbox + " AND status = 'PROCESSED'",
                        notifPattern), dataset.events().size());
        check(errors, "notificaciones con firma inválida",
                repository.countWhere("SELECT count(*) " + inbox + " AND signature_valid = false",
                        notifPattern), 0L);
        check(errors, "proveedores de notificación inválidos",
                repository.countWhere("SELECT count(*) " + inbox
                        + " AND provider <> 'MERCADO_PAGO'", notifPattern), 0L);
        check(errors, "notificaciones sin procesar",
                repository.countWhere("""
                        SELECT count(*) FROM payment_events WHERE notification_id LIKE ?
                        AND (attempts <> 1 OR processed_at IS NULL OR last_error IS NOT NULL)
                        """, notifPattern), 0L);

        String outboxJoin = """
                FROM outbox_events e
                JOIN payments p ON CAST(p.order_id AS varchar(64)) = e.aggregate_id
                WHERE p.idempotency_key LIKE ? AND e.payload LIKE ?
                """;
        check(errors, "eventos de infraestructura",
                repository.countWhere("SELECT count(*) " + outboxJoin,
                        idemPattern, sourcePattern), dataset.outbox().size());
        check(errors, "eventos no enviados",
                repository.countWhere("SELECT count(*) " + outboxJoin
                        + " AND e.status <> 'SENT'", idemPattern, sourcePattern), 0L);
        check(errors, "eventos sin sent_at",
                repository.countWhere("SELECT count(*) " + outboxJoin
                        + " AND e.sent_at IS NULL", idemPattern, sourcePattern), 0L);
        check(errors, "tipos de evento inválidos",
                repository.countWhere("SELECT count(*) " + outboxJoin
                        + " AND e.event_type NOT IN "
                        + "('PAYMENT_APPROVED','PAYMENT_REJECTED','PAYMENT_CANCELLED')",
                        idemPattern, sourcePattern), 0L);

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
