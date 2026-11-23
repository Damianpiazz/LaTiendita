package com.latiendita.paymentService.seeder;

import com.latiendita.paymentService.seeder.model.SeedDataset;
import com.latiendita.paymentService.seeder.model.SeededOutboxEvent;
import com.latiendita.paymentService.seeder.model.SeededPayment;
import com.latiendita.paymentService.seeder.model.SeededPaymentEvent;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Genera pagos, notificaciones de webhook y eventos de salida de forma
 * determinista. Las referencias cross-service se reconstruyen sin consultar
 * otras BDs: el pedido es `SEED-ORD-9200000+i` (order-service) y el cliente es
 * el UUID derivado de `SEED-CUST-9000000+i+1` (customer-service).
 */
@Component
public class PaymentDataGenerator {

    private static final String TOPIC = "payment-events";
    private static final String PROVIDER = "MERCADO_PAGO";
    private static final String CURRENCY = "ARS";
    private static final Instant BASE_TIME = Instant.parse("2024-04-01T00:00:00Z");

    private static final long PAYMENT_OFFSET = 400_000L;
    private static final long ORDER_OFFSET = 200_000L;

    private final SeederProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaymentDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Faker faker = new Faker(new Random(properties.getRandomSeed()));

        List<SeededPayment> payments = new ArrayList<>(properties.getCount());
        List<SeededPaymentEvent> events = new ArrayList<>(properties.getCount());
        List<SeededOutboxEvent> outbox = new ArrayList<>();

        for (int i = 0; i < properties.getCount(); i++) {
            long paymentId = base + PAYMENT_OFFSET + i;
            long orderId = base + ORDER_OFFSET + i;
            String orderNumber = prefix + "-ORD-" + padding(orderId);
            String customerId = customerId(prefix, base, i).toString();
            String providerPaymentId = prefix + "-PAY-" + padding(paymentId);
            String idempotencyKey = prefix + "-IDEM-" + padding(paymentId);
            String status = statusFor(i);
            Instant createdAt = BASE_TIME.plus(i, ChronoUnit.HOURS);
            Instant updatedAt = createdAt.plus(30, ChronoUnit.MINUTES);
            BigDecimal amount = BigDecimal.valueOf(
                    faker.number().numberBetween(199, 99_999), 2);
            boolean approved = "APPROVED".equals(status);
            boolean pending = "PENDING".equals(status);
            String statusDetail = approved ? null
                    : truncate("Sintético " + status + " " + orderNumber, 100);

            payments.add(new SeededPayment(
                    paymentId,
                    orderId,
                    customerId,
                    PROVIDER,
                    status,
                    amount,
                    CURRENCY,
                    truncate(prefix + "-PREF-" + padding(paymentId), 64),
                    truncate("https://tienda.test/checkout/" + orderNumber, 500),
                    truncate(orderNumber, 36),
                    truncate(idempotencyKey, 64),
                    truncate(providerPaymentId, 64),
                    approved ? "SEED-MP" : null,
                    approved ? "credit_card" : null,
                    statusDetail,
                    approved ? updatedAt : null,
                    pending || "EXPIRED".equals(status) ? updatedAt : null,
                    createdAt,
                    updatedAt,
                    0L));

            events.add(new SeededPaymentEvent(
                    PROVIDER,
                    truncate(prefix + "-NOTIF-" + padding(paymentId), 128),
                    "payment",
                    truncate(providerPaymentId, 64),
                    "payment.updated",
                    true,
                    "PROCESSED",
                    1,
                    null,
                    createdAt,
                    createdAt.plus(1, ChronoUnit.HOURS)));

            if (properties.isIncludeInfra()
                    && !"PENDING".equals(status) && !"EXPIRED".equals(status)) {
                appendOutbox(outbox, prefix, paymentId, orderId, orderNumber, customerId,
                        status, amount, statusDetail, providerPaymentId, approved,
                        createdAt.plus(45, ChronoUnit.MINUTES));
            }
        }

        return new SeedDataset(List.copyOf(payments), List.copyOf(events), List.copyOf(outbox));
    }

    private void appendOutbox(List<SeededOutboxEvent> outbox, String prefix,
                              long paymentId, long orderId, String orderNumber,
                              String customerId, String status, BigDecimal amount,
                              String statusDetail, String providerPaymentId,
                              boolean approved, Instant occurredAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", outboxType(status));
        payload.put("paymentId", paymentId);
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber);
        payload.put("customerId", customerId);
        payload.put("amount", amount);
        payload.put("currency", CURRENCY);
        payload.put("provider", PROVIDER);
        if (approved) {
            payload.put("providerPaymentId", providerPaymentId);
        } else if (statusDetail != null) {
            payload.put("statusDetail", statusDetail);
        }
        payload.put("occurredAt", occurredAt.toString());
        payload.put("source", prefix);

        outbox.add(new SeededOutboxEvent(TOPIC, String.valueOf(orderId), outboxType(status),
                json(payload), occurredAt, occurredAt.plus(1, ChronoUnit.HOURS)));
    }

    private static String outboxType(String status) {
        return switch (status) {
            case "APPROVED" -> "PAYMENT_APPROVED";
            case "REJECTED" -> "PAYMENT_REJECTED";
            case "CANCELLED" -> "PAYMENT_CANCELLED";
            default -> throw new IllegalArgumentException("Sin evento para el estado " + status);
        };
    }

    private static String json(Map<String, Object> payload) {
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el evento del seeder", ex);
        }
    }

    private static String statusFor(int i) {
        return switch (i % 10) {
            case 0, 1, 2, 3 -> "APPROVED";
            case 4 -> "PENDING";
            case 5, 6 -> "REJECTED";
            case 7 -> "CANCELLED";
            default -> "EXPIRED";
        };
    }

    private static UUID customerId(String prefix, long base, int index) {
        String key = prefix + "-CUST-" + padding(base + index + 1);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String padding(long value) {
        return String.format(Locale.ROOT, "%06d", value);
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
