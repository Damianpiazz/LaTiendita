package com.latiendita.customerService.seeder;

import com.latiendita.customerService.seeder.model.SeedDataset;
import com.latiendita.customerService.seeder.model.SeededCustomer;
import com.latiendita.customerService.seeder.model.SeededOutboxEvent;
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
 * Genera clientes sintéticos de forma determinista: la misma semilla, el mismo
 * `count` y el mismo prefijo producen siempre exactamente las mismas filas.
 *
 * <p>El UUID del cliente es una referencia cross-service: se deriva de
 * {@code <PREFIX>-CUST-<BASE+indice>} con {@link UUID#nameUUIDFromBytes(byte[])},
 * de modo que order/payment/shipment puedan reconstruir el mismo identificador
 * sin consultar esta base de datos.
 */
@Component
public class CustomerDataGenerator {

    private static final String TOPIC = "customer-events";
    private static final String EVENT_TYPE = "CUSTOMER_CREATED";
    private static final Instant BASE_TIME = Instant.parse("2024-01-01T00:00:00Z");

    private final SeederProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public CustomerDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Faker faker = new Faker(new Random(properties.getRandomSeed()));

        List<SeededCustomer> customers = new ArrayList<>(properties.getCount());
        List<SeededOutboxEvent> events = properties.isIncludeInfra()
                ? new ArrayList<>(properties.getCount())
                : new ArrayList<>();

        for (int i = 0; i < properties.getCount(); i++) {
            String sequence = padding(i + 1);
            String code = padding(base + i + 1);
            UUID id = UUID.nameUUIDFromBytes(
                    (prefix + "-CUST-" + code).getBytes(StandardCharsets.UTF_8));
            String sub = prefix + "-SUB-" + sequence;
            String email = prefix.toLowerCase(Locale.ROOT) + "+" + sequence + "@example.test";
            Instant createdAt = BASE_TIME.plus(i, ChronoUnit.DAYS);
            boolean newsletterOptIn = i % 3 == 0;
            String status = i % 13 == 0 ? "SUSPENDED" : "ACTIVE";

            SeededCustomer customer = new SeededCustomer(
                    id,
                    truncate(sub, 255),
                    truncate(email, 255),
                    truncate(faker.name().firstName(), 80),
                    truncate(faker.name().lastName(), 80),
                    "+34" + faker.number().numberBetween(100_000_000, 999_999_999),
                    status,
                    newsletterOptIn,
                    newsletterOptIn ? createdAt : null,
                    createdAt.plus(3, ChronoUnit.DAYS),
                    createdAt,
                    createdAt.plus(1, ChronoUnit.HOURS));
            customers.add(customer);

            if (properties.isIncludeInfra()) {
                events.add(new SeededOutboxEvent(
                        TOPIC,
                        id.toString(),
                        EVENT_TYPE,
                        payload(customer, prefix),
                        createdAt,
                        createdAt.plus(1, ChronoUnit.HOURS)));
            }
        }

        return new SeedDataset(List.copyOf(customers), List.copyOf(events));
    }

    private String payload(SeededCustomer customer, String prefix) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", EVENT_TYPE);
        values.put("id", customer.id());
        values.put("sub", customer.authorizerSub());
        values.put("email", customer.email());
        values.put("occurredAt", customer.createdAt().toString());
        values.put("source", prefix);
        try {
            return mapper.writeValueAsString(values);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el evento del seeder", ex);
        }
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
