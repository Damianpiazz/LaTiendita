package com.latiendita.orderService.seeder;

import com.latiendita.orderService.seeder.model.SeedDataset;
import com.latiendita.orderService.seeder.model.SeededOrder;
import com.latiendita.orderService.seeder.model.SeededOrderItem;
import com.latiendita.orderService.seeder.model.SeededOutboxEvent;
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
 * Genera pedidos, ítems y eventos de forma determinista: la misma semilla, el
 * mismo `count` y el mismo prefijo producen siempre exactamente las mismas filas.
 *
 * <p>Las referencias cross-service se reconstruyen sin consultar otras BDs:
 * <ul>
 *   <li>cliente: UUID de `<PREFIX>-CUST-<BASE+i+1>` (convención customer-service);</li>
 *   <li>producto: `<PREFIX>-SKU-<100000+i>` e id `BASE+100000+i` (convención product-service).</li>
 * </ul>
 */
@Component
public class OrderDataGenerator {

    private static final String TOPIC = "order-events";
    private static final Instant BASE_TIME = Instant.parse("2024-02-01T00:00:00Z");

    private static final long ORDER_OFFSET = 200_000L;
    private static final long ITEM_OFFSET = 300_000L;
    private static final long PRODUCT_OFFSET = 100_000L;

    private final SeederProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrderDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Faker faker = new Faker(new Random(properties.getRandomSeed()));

        List<SeededOrder> orders = new ArrayList<>(properties.getCount());
        List<SeededOrderItem> items = new ArrayList<>(properties.getCount() * 2);
        List<SeededOutboxEvent> events = new ArrayList<>();
        long itemSequence = 0;

        for (int i = 0; i < properties.getCount(); i++) {
            long orderId = base + ORDER_OFFSET + i;
            String orderNumber = prefix + "-ORD-" + padding(orderId);
            String customerId = customerId(prefix, base, i % properties.getCount()).toString();
            Instant createdAt = BASE_TIME.plus(i, ChronoUnit.DAYS);
            Instant updatedAt = createdAt.plus(6, ChronoUnit.HOURS);
            String status = statusFor(i);
            boolean cancelled = "CANCELLED".equals(status);
            boolean refundRequired = cancelled && i % 2 == 0;
            String cancelReason = cancelled
                    ? truncate("Cancelado por prueba sintética " + orderNumber, 500)
                    : null;

            int maxItems = Math.min(3, properties.getCount());
            int itemCount = 1 + (i % maxItems);
            BigDecimal total = BigDecimal.ZERO;
            List<Map<String, Object>> eventItems = new ArrayList<>(itemCount);

            for (int j = 0; j < itemCount; j++) {
                int productIndex = (i + j) % properties.getCount();
                long productId = base + PRODUCT_OFFSET + productIndex;
                String sku = prefix + "-SKU-" + padding(PRODUCT_OFFSET + productIndex);
                BigDecimal unitPrice = BigDecimal.valueOf(
                        faker.number().numberBetween(199, 99_999), 2);
                int quantity = faker.number().numberBetween(1, 6);
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

                items.add(new SeededOrderItem(
                        base + ITEM_OFFSET + itemSequence++,
                        orderId,
                        productId,
                        truncate("Producto sintético " + sku, 255),
                        truncate(sku, 100),
                        quantity,
                        unitPrice,
                        subtotal));
                total = total.add(subtotal);
                eventItems.add(Map.of(
                        "productId", productId,
                        "quantity", quantity,
                        "unitPrice", unitPrice,
                        "subtotal", subtotal));
            }

            orders.add(new SeededOrder(
                    orderId,
                    truncate(orderNumber, 36),
                    customerId,
                    status,
                    total,
                    cancelReason,
                    refundRequired,
                    createdAt,
                    updatedAt,
                    0L));

            if (properties.isIncludeInfra()) {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("orderId", orderId);
                created.put("orderNumber", orderNumber);
                created.put("customerId", customerId);
                created.put("total", total);
                created.put("couponCode", null);
                created.put("items", eventItems);
                created.put("occurredAt", createdAt.toString());
                created.put("source", prefix);
                events.add(event(orderId, "ORDER_CREATED", created, createdAt));

                appendStatusEvents(events, prefix, orderId, orderNumber, customerId,
                        status, refundRequired, cancelReason, total, updatedAt);
            }
        }

        return new SeedDataset(List.copyOf(orders), List.copyOf(items), List.copyOf(events));
    }

    private static void appendStatusEvents(List<SeededOutboxEvent> events, String prefix,
                                           long orderId, String orderNumber, String customerId,
                                           String status, boolean refundRequired,
                                           String cancelReason, BigDecimal amount,
                                           Instant occurredAt) {
        if (!"PENDING".equals(status) && !"CANCELLED".equals(status)) {
            List<String> targets = switch (status) {
                case "PAID" -> List.of("PAID");
                case "SHIPPED" -> List.of("PAID", "SHIPPED");
                default -> List.of("PAID", "SHIPPED", "DELIVERED");
            };
            String from = "PENDING";
            for (String target : targets) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("orderId", orderId);
                payload.put("orderNumber", orderNumber);
                payload.put("customerId", customerId);
                payload.put("from", from);
                payload.put("to", target);
                payload.put("occurredAt", occurredAt.toString());
                payload.put("source", prefix);
                events.add(event(orderId, "ORDER_STATUS_CHANGED", payload, occurredAt));
                from = target;
            }
            return;
        }

        if ("CANCELLED".equals(status)) {
            String from = refundRequired ? "PAID" : "PENDING";
            Map<String, Object> cancelled = new LinkedHashMap<>();
            cancelled.put("orderId", orderId);
            cancelled.put("orderNumber", orderNumber);
            cancelled.put("customerId", customerId);
            cancelled.put("reason", cancelReason);
            cancelled.put("refundRequired", refundRequired);
            cancelled.put("occurredAt", occurredAt.toString());
            cancelled.put("source", prefix);
            events.add(event(orderId, "ORDER_CANCELLED", cancelled, occurredAt));

            if (refundRequired) {
                Map<String, Object> refund = new LinkedHashMap<>();
                refund.put("orderId", orderId);
                refund.put("orderNumber", orderNumber);
                refund.put("customerId", customerId);
                refund.put("amount", amount);
                refund.put("occurredAt", occurredAt.toString());
                refund.put("source", prefix);
                events.add(event(orderId, "REFUND_REQUESTED", refund, occurredAt));
            }
        }
    }

    private static SeededOutboxEvent event(long orderId, String eventType,
                                           Map<String, Object> payload, Instant occurredAt) {
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

    private static String statusFor(int i) {
        return switch (i % 10) {
            case 0, 1 -> "CANCELLED";
            case 2, 3 -> "PENDING";
            case 4, 5 -> "PAID";
            case 6, 7 -> "SHIPPED";
            default -> "DELIVERED";
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
