package com.latiendita.shipmentService.seeder;

import com.latiendita.shipmentService.seeder.model.SeedDataset;
import com.latiendita.shipmentService.seeder.model.SeededAddress;
import com.latiendita.shipmentService.seeder.model.SeededOutboxEvent;
import com.latiendita.shipmentService.seeder.model.SeededShipment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Genera envíos, direcciones y eventos de forma determinista: la misma semilla,
 * el mismo `count` y el mismo prefijo producen siempre exactamente las mismas filas.
 *
 * <p>Las referencias cross-service se reconstruyen sin consultar otras BDs:
 * <ul>
 *   <li>orderId: `<PREFIX>-ORD-<BASE+200000+i>` (convención order-service);</li>
 *   <li>customerId: `<PREFIX>-CUST-<BASE+i+1>` (clave sintética compartida).</li>
 * </ul>
 */
@Component
public class ShipmentDataGenerator {

    private static final String TOPIC = "shipment-events";
    private static final Instant BASE_TIME = Instant.parse("2024-02-01T00:00:00Z");
    private static final LocalDate BASE_DATE = LocalDate.parse("2024-02-01");

    private static final long ORDER_OFFSET = 200_000L;
    private static final long SHIPMENT_OFFSET = 500_000L;
    private static final long ADDRESS_OFFSET = 600_000L;

    private static final List<String> STATUSES = List.of(
            "AWAITING_PAYMENT", "PENDING_DISPATCH", "DISPATCHED", "DELIVERED", "CANCELLED");

    private final SeederProperties properties;

    public ShipmentDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Random random = new Random(properties.getRandomSeed());

        List<SeededShipment> shipments = new ArrayList<>(properties.getCount());
        List<SeededAddress> addresses = new ArrayList<>(properties.getCount());
        List<SeededOutboxEvent> events = new ArrayList<>();

        for (int i = 0; i < properties.getCount(); i++) {
            long orderId = base + ORDER_OFFSET + i;
            String customerId = customerKey(prefix, base, i);
            long shipmentId = base + SHIPMENT_OFFSET + i;
            long addressId = base + ADDRESS_OFFSET + i;
            Instant createdAt = BASE_TIME.plus(i, ChronoUnit.DAYS);
            Instant updatedAt = createdAt.plus(6, ChronoUnit.HOURS);
            String status = STATUSES.get(i % STATUSES.size());
            boolean dispatched = "DISPATCHED".equals(status) || "DELIVERED".equals(status);
            boolean delivered = "DELIVERED".equals(status);
            Instant dispatchedAt = dispatched ? updatedAt : null;
            Instant deliveredAt = delivered ? updatedAt.plus(1, ChronoUnit.HOURS) : null;
            LocalDate estimatedFrom = BASE_DATE.plusDays(i);
            LocalDate estimatedTo = estimatedFrom.plusDays(3 + (i % 3));
            BigDecimal shippingCost = BigDecimal.valueOf(random.nextInt(9_800) + 199, 2);
            String phone = "+54911" + String.format(Locale.ROOT, "%08d",
                    random.nextInt(100_000_000));
            String recipientName = truncate("Destinatario sintético " + pad(orderId), 120);
            String addressLine = truncate("Calle sintética " + (i + 1), 200);
            String references = "Puerta " + (i % 9 + 1);

            SeededAddress address = new SeededAddress(
                    addressId, customerId, recipientName, phone, addressLine,
                    "Buenos Aires", "CABA", "1000", references, true, createdAt, updatedAt);
            SeededShipment shipment = new SeededShipment(
                    shipmentId, orderId, customerId, status, shippingCost, "ARS",
                    recipientName, phone, addressLine, "Buenos Aires", "CABA", "1000",
                    references, estimatedFrom, estimatedTo, dispatchedAt, deliveredAt,
                    createdAt, updatedAt, 0L);

            addresses.add(address);
            shipments.add(shipment);
            if (properties.isIncludeInfra()) {
                appendStatusEvents(events, prefix, shipment);
            }
        }

        return new SeedDataset(List.copyOf(shipments), List.copyOf(addresses),
                List.copyOf(events));
    }

    private static void appendStatusEvents(List<SeededOutboxEvent> events, String prefix,
                                           SeededShipment shipment) {
        String status = shipment.status();
        if (!"DISPATCHED".equals(status) && !"DELIVERED".equals(status)
                && !"CANCELLED".equals(status)) {
            return;
        }

        Instant occurredAt = shipment.updatedAt();
        if ("CANCELLED".equals(status)) {
            events.add(event(shipment.orderId(), "SHIPMENT_CANCELLED",
                    commonPayload(prefix, shipment), occurredAt));
            return;
        }

        Map<String, Object> dispatched = commonPayload(prefix, shipment);
        dispatched.put("dispatchedAt", shipment.dispatchedAt().toString());
        dispatched.put("estimatedTo", shipment.estimatedTo().toString());
        events.add(event(shipment.orderId(), "SHIPMENT_DISPATCHED", dispatched, occurredAt));

        if ("DELIVERED".equals(status)) {
            Map<String, Object> delivered = commonPayload(prefix, shipment);
            delivered.put("deliveredAt", shipment.deliveredAt().toString());
            events.add(event(shipment.orderId(), "SHIPMENT_DELIVERED", delivered,
                    shipment.deliveredAt()));
        }
    }

    private static Map<String, Object> commonPayload(String prefix, SeededShipment shipment) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shipmentId", shipment.id());
        payload.put("orderId", shipment.orderId());
        payload.put("customerId", shipment.customerId());
        payload.put("occurredAt", shipment.updatedAt().toString());
        payload.put("source", prefix);
        return payload;
    }

    private static SeededOutboxEvent event(long orderId, String eventType,
                                           Map<String, Object> payload, Instant occurredAt) {
        payload.put("type", eventType);
        return new SeededOutboxEvent(TOPIC, String.valueOf(orderId), eventType,
                json(payload), occurredAt, occurredAt.plus(1, ChronoUnit.HOURS));
    }

    private static String json(Map<String, Object> payload) {
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el evento del seeder", ex);
        }
    }

    private static String customerKey(String prefix, long base, int index) {
        return prefix + "-CUST-" + pad(base + index + 1);
    }

    private static String pad(long value) {
        return String.format(Locale.ROOT, "%06d", value);
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
