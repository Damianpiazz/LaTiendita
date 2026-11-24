package com.latiendita.cartService.seeder;

import com.latiendita.cartService.seeder.model.SeedDataset;
import com.latiendita.cartService.seeder.model.SeededCart;
import com.latiendita.cartService.seeder.model.SeededCartItem;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

/**
 * Genera carritos e ítems de forma determinista. Cada carrito pertenece a un
 * cliente sintético de customer-service y sus productos son los de
 * product-service; las referencias se reconstruyen sin consultar otras BDs ni
 * hacer HTTP.
 */
@Component
public class CartDataGenerator {

    private static final Instant BASE_TIME = Instant.parse("2024-05-01T00:00:00Z");
    private static final long PRODUCT_BASE = 100_000L;
    private static final long PRODUCT_COUNT = 1_000L;

    private final SeederProperties properties;

    public CartDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Faker faker = new Faker(new Random(properties.getRandomSeed()));

        List<SeededCart> carts = new ArrayList<>(properties.getCount());
        List<SeededCartItem> items = new ArrayList<>(properties.getCount() * 2);

        for (int i = 0; i < properties.getCount(); i++) {
            long sequence = base + i + 1;
            UUID cartId = syntheticUuid(prefix, "CART", sequence);
            String userId = syntheticUuid(prefix, "CUST", sequence).toString();
            Instant createdAt = BASE_TIME.plus(i, ChronoUnit.HOURS);
            Instant updatedAt = createdAt.plus(15, ChronoUnit.MINUTES);

            carts.add(new SeededCart(cartId, userId, createdAt, updatedAt, 0L));

            int itemsInCart = 1 + (i % 3);
            for (int j = 0; j < itemsInCart; j++) {
                long productIndex = ((long) i * itemsInCart + j) % PRODUCT_COUNT;
                long productId = 9_100_000L + productIndex;
                String sku = prefix + "-SKU-" + padding(PRODUCT_BASE + productIndex);
                BigDecimal unitPrice = BigDecimal.valueOf(
                        faker.number().numberBetween(199, 99_999), 2);

                items.add(new SeededCartItem(
                        syntheticUuid(prefix, "ITEM", sequence * 10L + j),
                        cartId,
                        productId,
                        faker.number().numberBetween(1, 10),
                        unitPrice,
                        "Producto sintético " + sku,
                        "https://tienda.test/images/" + sku + ".png",
                        j == 0 && i % 10 == 0,
                        createdAt,
                        updatedAt.plus(j, ChronoUnit.MINUTES)));
            }
        }

        return new SeedDataset(List.copyOf(carts), List.copyOf(items));
    }

    private static UUID syntheticUuid(String prefix, String kind, long sequence) {
        String key = prefix + "-" + kind + "-" + padding(sequence);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String padding(long value) {
        return String.format(Locale.ROOT, "%06d", value);
    }
}
