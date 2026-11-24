package com.latiendita.offerService.seeder;

import com.latiendita.offerService.seeder.model.SeedDataset;
import com.latiendita.offerService.seeder.model.SeededBenefit;
import com.latiendita.offerService.seeder.model.SeededCouponUsage;
import com.latiendita.offerService.seeder.model.SeededOutboxEvent;
import com.latiendita.offerService.seeder.model.SeededPromotion;
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
 * Genera promociones, usos de cupón, beneficios y eventos de infraestructura de
 * forma determinista. Las referencias a pedidos, clientes y productos se
 * reconstruyen con la misma convención de los demás seeders, sin consultar
 * otras BDs ni hacer HTTP.
 */
@Component
public class OfferDataGenerator {

    static final String PROMOTION_TOPIC = "promotion-events";
    static final String PROMOTION_CHANGED = "PROMOTION_CHANGED";

    private static final Instant BASE_TIME = Instant.parse("2024-06-01T00:00:00Z");
    private static final int BENEFIT_COUNT = 20;
    private static final long PRODUCT_BASE = 9_100_000L;
    private static final long PRODUCT_COUNT = 1_000L;
    private static final long CATEGORY_BASE = 20L;
    private static final long CATEGORY_COUNT = 20L;
    private static final long ORDER_ID_BASE = 9_200_000L;

    /** Promociones no FEATURED entre 1.000 índices (334 son FEATURED). */
    private static final int NON_FEATURED_COUNT = 666;

    private final SeederProperties properties;

    public OfferDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Faker faker = new Faker(new Random(properties.getRandomSeed()));

        List<SeededPromotion> promotions = new ArrayList<>(properties.getCount());
        for (int i = 0; i < properties.getCount(); i++) {
            promotions.add(promotion(prefix, base, i, faker));
        }

        List<Integer> couponIndices = new ArrayList<>(NON_FEATURED_COUNT);
        for (int i = 0; i < promotions.size(); i++) {
            if (!promotions.get(i).type().equals("FEATURED")) {
                couponIndices.add(i);
            }
        }
        if (couponIndices.isEmpty()) {
            throw new IllegalStateException(
                    "El seeder necesita al menos una promoción no FEATURED para los usos");
        }

        List<SeededCouponUsage> usages = new ArrayList<>(properties.getCount());
        for (int i = 0; i < properties.getCount(); i++) {
            long sequence = base + i + 1;
            usages.add(new SeededCouponUsage(
                    syntheticUuid(prefix, "USAGE", sequence),
                    promotions.get(couponIndices.get(i % couponIndices.size())).id(),
                    prefix + "-CUST-" + padding(sequence),
                    ORDER_ID_BASE + i,
                    BASE_TIME.plus(i, ChronoUnit.MINUTES)));
        }

        List<SeededBenefit> benefits = new ArrayList<>(BENEFIT_COUNT);
        for (int i = 0; i < BENEFIT_COUNT; i++) {
            long sequence = base + i + 1;
            String code = prefix + "-BENEFIT-" + padding(i + 1);
            benefits.add(new SeededBenefit(
                    syntheticUuid(prefix, "BENEFIT", sequence),
                    code,
                    "Beneficio sintético " + padding(i + 1),
                    "Beneficio de prueba generado por offer-service seeder (" + code + ")",
                    "icon-seed-" + padding(i + 1),
                    i + 1,
                    true,
                    BASE_TIME,
                    BASE_TIME.plus(1, ChronoUnit.HOURS)));
        }

        List<SeededOutboxEvent> events = new ArrayList<>();
        if (properties.isIncludeInfra()) {
            for (SeededPromotion promotion : promotions) {
                events.add(event(promotion));
            }
        }

        return new SeedDataset(List.copyOf(promotions), List.copyOf(usages),
                List.copyOf(benefits), List.copyOf(events));
    }

    private static SeededPromotion promotion(String prefix, long base, int i, Faker faker) {
        long sequence = base + i + 1;
        String type = switch (i % 3) {
            case 0 -> "FEATURED";
            case 1 -> "PERCENTAGE";
            default -> "FIXED_AMOUNT";
        };
        String scope = switch (i % 3) {
            case 0 -> "PRODUCT";
            case 1 -> "CATEGORY";
            default -> "ORDER";
        };

        Long productId = scope.equals("PRODUCT")
                ? PRODUCT_BASE + (i % PRODUCT_COUNT) : null;
        Long categoryId = scope.equals("CATEGORY")
                ? CATEGORY_BASE + (i % CATEGORY_COUNT) : null;
        BigDecimal discountValue = switch (type) {
            case "PERCENTAGE" -> BigDecimal.valueOf(faker.number().numberBetween(5, 40));
            case "FIXED_AMOUNT" -> BigDecimal.valueOf(
                    faker.number().numberBetween(500, 10_000), 2);
            default -> null;
        };
        BigDecimal minSubtotal = type.equals("FEATURED") ? null : BigDecimal.valueOf(100, 2);
        Instant startsAt = BASE_TIME.minus(i % 30, ChronoUnit.DAYS);
        Instant endsAt = BASE_TIME.plus(365L + (i % 30), ChronoUnit.DAYS);

        return new SeededPromotion(
                syntheticUuid(prefix, "PROMO", sequence),
                prefix + "-PROMO-" + padding(i + 1),
                "Promoción sintética " + padding(i + 1),
                type,
                scope,
                productId,
                categoryId,
                discountValue,
                minSubtotal,
                startsAt,
                endsAt,
                1 + (i % 10),
                i % 2 == 0,
                i % 10 != 9,
                startsAt,
                startsAt.plus(1, ChronoUnit.HOURS));
    }

    private static SeededOutboxEvent event(SeededPromotion promotion) {
        String targetId = switch (promotion.scope()) {
            case "PRODUCT" -> String.valueOf(promotion.productId());
            case "CATEGORY" -> String.valueOf(promotion.categoryId());
            default -> "null";
        };
        String payload = "{\"type\":\"" + PROMOTION_CHANGED + "\","
                + "\"promotionId\":\"" + promotion.id() + "\","
                + "\"scope\":\"" + promotion.scope() + "\","
                + "\"targetId\":" + targetId + ","
                + "\"active\":" + promotion.active() + ","
                + "\"occurredAt\":\"" + promotion.createdAt() + "\","
                + "\"source\":\"SEED\"}";
        return new SeededOutboxEvent(
                PROMOTION_TOPIC,
                promotion.id().toString(),
                PROMOTION_CHANGED,
                payload,
                "SENT",
                1,
                promotion.createdAt(),
                promotion.createdAt().plus(2, ChronoUnit.MINUTES));
    }

    private static UUID syntheticUuid(String prefix, String kind, long sequence) {
        String key = prefix + "-" + kind + "-" + padding(sequence);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String padding(long value) {
        return String.format(Locale.ROOT, "%06d", value);
    }
}
