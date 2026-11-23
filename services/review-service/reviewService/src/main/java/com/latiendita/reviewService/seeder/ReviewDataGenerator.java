package com.latiendita.reviewService.seeder;

import com.latiendita.reviewService.seeder.model.SeedDataset;
import com.latiendita.reviewService.seeder.model.SeededOutboxEvent;
import com.latiendita.reviewService.seeder.model.SeededProductRating;
import com.latiendita.reviewService.seeder.model.SeededReview;
import com.latiendita.reviewService.seeder.model.SeededVote;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Genera reseñas, votos útiles, agregados de calificación y eventos de
 * infraestructura de forma determinista.
 *
 * <p>Para mantener la coherencia con order-service, cada reseña usa un pedido
 * con estado elegible ({@code PAID}, {@code SHIPPED} o {@code DELIVERED}) y un
 * producto que realmente aparece en los ítems de ese pedido. Las referencias
 * cross-service se reconstruyen con la misma convención de los demás seeders,
 * sin consultar otras BDs ni hacer HTTP.
 */
@Component
public class ReviewDataGenerator {

    static final String REVIEW_TOPIC = "review-events";
    static final String REVIEW_APPROVED = "REVIEW_APPROVED";
    static final String EVENT_SOURCE = "SEED";

    private static final Instant BASE_TIME = Instant.parse("2024-07-01T00:00:00Z");
    private static final long PRODUCT_OFFSET = 100_000L;
    private static final long ORDER_OFFSET = 200_000L;
    private static final long VOTER_OFFSET = 1_000_000L;
    private static final int VOTES_EVERY_N_APPROVED = 5;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SeederProperties properties;

    public ReviewDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        int count = properties.getCount();
        List<int[]> purchases = eligiblePurchases(count);
        if (purchases.size() < count) {
            throw new IllegalStateException("El seeder de review necesita " + count
                    + " compras elegibles, pero el convenio de order-service sólo genera "
                    + purchases.size() + " con count=" + count);
        }

        Faker faker = new Faker(new Random(properties.getRandomSeed()));
        List<SeededReview> reviews = new ArrayList<>(count);
        List<SeededVote> votes = new ArrayList<>(count / VOTES_EVERY_N_APPROVED + 1);
        int approvedSeen = 0;

        for (int i = 0; i < count; i++) {
            int[] purchase = purchases.get(i);
            int orderIndex = purchase[0];
            int itemIndex = purchase[1];
            long sequence = base + i + 1;
            UUID reviewId = syntheticUuid(prefix, "REVIEW", sequence);
            String customerId = customerId(prefix, base, orderIndex).toString();
            long productId = base + PRODUCT_OFFSET + ((orderIndex + itemIndex) % count);
            long orderId = base + ORDER_OFFSET + orderIndex;
            int rating = 1 + (i % 5);
            String status = statusFor(i);
            boolean approved = "approved".equals(status);
            boolean rejected = "rejected".equals(status);
            if (approved) {
                approvedSeen++;
            }

            int helpfulVotes = approved && approvedSeen % VOTES_EVERY_N_APPROVED == 0 ? 1 : 0;
            Instant createdAt = BASE_TIME.plus(i, ChronoUnit.MINUTES);
            Instant updatedAt = createdAt.plus(1, ChronoUnit.HOURS);
            String moderationReason = rejected
                    ? truncate("Rechazada por moderación sintética " + padding(sequence), 500)
                    : null;

            reviews.add(new SeededReview(
                    reviewId,
                    productId,
                    customerId,
                    orderId,
                    rating,
                    truncate(faker.book().title(), 120),
                    truncate(faker.lorem().sentence(18), 4000),
                    status,
                    helpfulVotes,
                    moderationReason,
                    createdAt,
                    updatedAt));

            if (helpfulVotes > 0) {
                String voter = UUID.nameUUIDFromBytes((prefix + "-VOTER-"
                        + padding(base + VOTER_OFFSET + i + 1))
                        .getBytes(StandardCharsets.UTF_8)).toString();
                votes.add(new SeededVote(
                        syntheticUuid(prefix, "VOTE", sequence),
                        reviewId,
                        voter,
                        createdAt.plus(2, ChronoUnit.HOURS)));
            }
        }

        List<SeededProductRating> ratings = aggregateRatings(reviews);
        List<SeededOutboxEvent> events = new ArrayList<>();
        if (properties.isIncludeInfra()) {
            Map<Long, SeededProductRating> byProduct = new LinkedHashMap<>();
            for (SeededProductRating rating : ratings) {
                byProduct.put(rating.productId(), rating);
            }
            for (SeededReview review : reviews) {
                if ("approved".equals(review.status())) {
                    events.add(event(review, byProduct.get(review.productId())));
                }
            }
        }

        return new SeedDataset(List.copyOf(reviews), List.copyOf(votes),
                List.copyOf(ratings), List.copyOf(events));
    }

    /**
     * Réplicas las compras que order-service considera válidas para reseñar:
     * estados elegibles y sólo los ítems que realmente tiene cada pedido.
     */
    private static List<int[]> eligiblePurchases(int count) {
        List<int[]> purchases = new ArrayList<>(count);
        for (int orderIndex = 0; orderIndex < count; orderIndex++) {
            if (!eligibleOrder(orderIndex)) {
                continue;
            }
            int itemCount = 1 + (orderIndex % 3);
            for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                purchases.add(new int[]{orderIndex, itemIndex});
                if (purchases.size() == count) {
                    return List.copyOf(purchases);
                }
            }
        }
        return List.copyOf(purchases);
    }

    private static boolean eligibleOrder(int orderIndex) {
        return orderIndex % 10 >= 4;
    }

    private static String statusFor(int i) {
        return switch (i % 10) {
            case 0, 1, 2, 3, 4, 5 -> "approved";
            case 6, 7 -> "pending";
            default -> "rejected";
        };
    }

    private static List<SeededProductRating> aggregateRatings(List<SeededReview> reviews) {
        Map<Long, List<SeededReview>> approvedByProduct = new LinkedHashMap<>();
        for (SeededReview review : reviews) {
            if ("approved".equals(review.status())) {
                approvedByProduct.computeIfAbsent(review.productId(), key -> new ArrayList<>())
                        .add(review);
            }
        }

        List<SeededProductRating> ratings = new ArrayList<>(approvedByProduct.size());
        for (Map.Entry<Long, List<SeededReview>> entry : approvedByProduct.entrySet()) {
            List<SeededReview> approved = entry.getValue();
            Map<String, Integer> distribution = emptyDistribution();
            BigDecimal sum = BigDecimal.ZERO;
            Instant updatedAt = approved.get(0).updatedAt();
            for (SeededReview review : approved) {
                distribution.merge(String.valueOf(review.rating()), 1, Integer::sum);
                sum = sum.add(BigDecimal.valueOf(review.rating()));
                if (review.updatedAt().isAfter(updatedAt)) {
                    updatedAt = review.updatedAt();
                }
            }
            BigDecimal average = sum.divide(BigDecimal.valueOf(approved.size()), 2,
                    RoundingMode.HALF_UP);
            ratings.add(new SeededProductRating(
                    entry.getKey(), average, approved.size(), json(distribution), updatedAt));
        }
        return List.copyOf(ratings);
    }

    private static SeededOutboxEvent event(SeededReview review, SeededProductRating rating) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", REVIEW_APPROVED);
        values.put("reviewId", review.id());
        values.put("productId", review.productId());
        values.put("rating", review.rating());
        values.put("average", rating.average());
        values.put("reviewCount", rating.reviewCount());
        values.put("occurredAt", review.updatedAt().toString());
        values.put("source", EVENT_SOURCE);
        return new SeededOutboxEvent(
                REVIEW_TOPIC,
                review.id().toString(),
                REVIEW_APPROVED,
                json(values),
                "SENT",
                1,
                review.createdAt(),
                review.updatedAt().plus(1, ChronoUnit.MINUTES));
    }

    private static Map<String, Integer> emptyDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            distribution.put(String.valueOf(star), 0);
        }
        return distribution;
    }

    private static String json(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el evento del seeder", ex);
        }
    }

    private static UUID syntheticUuid(String prefix, String kind, long sequence) {
        String key = prefix + "-" + kind + "-" + padding(sequence);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
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
