package com.latiendita.reviewService.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.latiendita.reviewService.seeder.model.SeedDataset;
import com.latiendita.reviewService.seeder.model.SeededOutboxEvent;
import com.latiendita.reviewService.seeder.model.SeededProductRating;
import com.latiendita.reviewService.seeder.model.SeededReview;
import com.latiendita.reviewService.seeder.model.SeededVote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ReviewDataGeneratorTest {

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(30, 42, true).generate())
                .isEqualTo(generator(30, 42, true).generate());
    }

    @Test
    void shouldChangeTextWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(30, 42, true).generate();
        SeedDataset second = generator(30, 43, true).generate();

        assertThat(second.reviews().get(0).body()).isNotEqualTo(first.reviews().get(0).body());
        assertThat(second.reviews().get(0).id()).isEqualTo(first.reviews().get(0).id());
        assertThat(second.reviews().get(0).customerId())
                .isEqualTo(first.reviews().get(0).customerId());
        assertThat(second.reviews().get(0).orderId()).isEqualTo(first.reviews().get(0).orderId());
    }

    @Test
    void shouldRespectVolumeStatesAggregatesAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, true).generate();

        assertThat(dataset.reviews()).hasSize(1000);
        assertThat(dataset.votes()).hasSize(120);
        assertThat(dataset.events()).hasSize(600);
        assertThat(countStatus(dataset, "approved")).isEqualTo(600);
        assertThat(countStatus(dataset, "pending")).isEqualTo(200);
        assertThat(countStatus(dataset, "rejected")).isEqualTo(200);

        assertThat(unique(dataset.reviews().stream().map(SeededReview::id))).hasSize(1000);
        assertThat(unique(dataset.reviews().stream()
                .map(r -> r.customerId() + ":" + r.productId()))).hasSize(1000);
        assertThat(unique(dataset.votes().stream().map(SeededVote::id))).hasSize(120);
        assertThat(unique(dataset.votes().stream()
                .map(v -> v.reviewId() + ":" + v.customerId()))).hasSize(120);
        assertThat(unique(dataset.events().stream().map(SeededOutboxEvent::aggregateId)))
                .hasSize(600);

        Set<UUID> approvedIds = dataset.reviews().stream()
                .filter(r -> "approved".equals(r.status()))
                .map(SeededReview::id).collect(Collectors.toSet());

        for (SeededReview review : dataset.reviews()) {
            assertThat(review.rating()).isBetween(1, 5);
            assertThat(review.status()).isIn("approved", "pending", "rejected");
            assertThat(review.title()).isNotBlank().hasSizeLessThanOrEqualTo(120);
            assertThat(review.body()).isNotBlank();
            assertThat(review.helpfulVotes()).isBetween(0, 1);
            if ("rejected".equals(review.status())) {
                assertThat(review.moderationReason()).isNotBlank().hasSizeLessThanOrEqualTo(500);
            } else {
                assertThat(review.moderationReason()).isNull();
            }
            assertThat(review.productId()).isBetween(9_100_000L, 9_100_999L);
            long orderIndex = review.orderId() - 9_200_000L;
            assertThat(orderIndex).isBetween(0L, 999L);
            assertThat(orderIndex % 10).isGreaterThanOrEqualTo(4);
            Set<Long> productsInOrder = productsInOrder((int) orderIndex);
            assertThat(productsInOrder).contains(review.productId());
            assertThat(review.customerId()).isEqualTo(customerId(9_000_000L, (int) orderIndex));
        }

        assertThat(dataset.votes()).allSatisfy(vote -> {
            assertThat(approvedIds).contains(vote.reviewId());
            assertThat(dataset.reviews().stream()
                    .filter(r -> r.id().equals(vote.reviewId()))
                    .findFirst().orElseThrow().customerId())
                    .isNotEqualTo(vote.customerId());
        });
        assertThat(dataset.votes()).extracting(SeededVote::customerId)
                .doesNotHaveDuplicates();
        assertThat(dataset.reviews().stream().mapToInt(SeededReview::helpfulVotes).sum())
                .isEqualTo(dataset.votes().size());

        Map<Long, List<SeededReview>> approvedByProduct = dataset.reviews().stream()
                .filter(r -> "approved".equals(r.status()))
                .collect(Collectors.groupingBy(SeededReview::productId));
        assertThat(dataset.ratings()).hasSize(approvedByProduct.size());
        assertThat(dataset.ratings()).extracting(SeededProductRating::productId)
                .doesNotHaveDuplicates();

        for (SeededProductRating rating : dataset.ratings()) {
            List<SeededReview> approved = approvedByProduct.get(rating.productId());
            assertThat(rating.reviewCount()).isEqualTo(approved.size());
            BigDecimal expectedAverage = approved.stream()
                    .map(r -> BigDecimal.valueOf(r.rating()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(approved.size()), 2, RoundingMode.HALF_UP);
            assertThat(rating.average()).isEqualByComparingTo(expectedAverage);
            assertThat(rating.distribution()).contains("\"1\"", "\"2\"", "\"3\"", "\"4\"", "\"5\"");
            for (int star = 1; star <= 5; star++) {
                final int currentStar = star;
                long expected = approved.stream()
                        .filter(r -> r.rating() == currentStar).count();
                assertThat(parseDistribution(rating.distribution()).get(String.valueOf(star)))
                        .isEqualTo((int) expected);
            }
        }

        assertThat(dataset.events()).allSatisfy(event -> {
            assertThat(event.topic()).isEqualTo("review-events");
            assertThat(event.eventType()).isEqualTo("REVIEW_APPROVED");
            assertThat(event.status()).isEqualTo("SENT");
            assertThat(event.attempts()).isEqualTo(1);
            assertThat(event.sentAt()).isAfter(event.createdAt());
            assertThat(event.payload()).contains("\"source\":\"SEED\"")
                    .contains("REVIEW_APPROVED");
            assertThat(approvedIds).contains(UUID.fromString(event.aggregateId()));
            assertThat(event.payload()).contains(event.aggregateId());
        });
    }

    @Test
    void shouldDeriveCrossServiceIdsAndValidateMinimumVolume() {
        SeedDataset dataset = generator(30, 42, true).generate();
        SeededReview first = dataset.reviews().get(0);

        assertThat(first.id()).isEqualTo(syntheticUuid("SEED-REVIEW-9000001"));
        assertThat(first.customerId()).isEqualTo(customerId(9_000_000L, 4));
        assertThat(first.productId()).isEqualTo(9_100_004L);
        assertThat(first.orderId()).isEqualTo(9_200_004L);
        assertThat(dataset.votes()).hasSize(3);
        assertThat(generator(30, 42, false).generate().events()).isEmpty();

        assertThatThrownBy(() -> generator(3, 42, true).generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("compras elegibles");
    }

    private static long countStatus(SeedDataset dataset, String status) {
        return dataset.reviews().stream().filter(r -> r.status().equals(status)).count();
    }

    private static Set<Long> productsInOrder(int orderIndex) {
        Set<Long> products = new java.util.LinkedHashSet<>();
        int itemCount = 1 + (orderIndex % 3);
        for (int j = 0; j < itemCount; j++) {
            products.add(9_100_000L + ((orderIndex + j) % 1000));
        }
        return products;
    }

    private static Map<String, Integer> parseDistribution(String json) {
        Map<String, Integer> values = new HashMap<>();
        for (String part : json.replace("{", "").replace("}", "").split(",")) {
            String[] pair = part.split(":");
            values.put(pair[0].replace("\"", "").trim(),
                    Integer.parseInt(pair[1].trim()));
        }
        return values;
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static UUID syntheticUuid(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String customerId(long base, int index) {
        String key = "SEED-CUST-" + String.format(java.util.Locale.ROOT, "%06d", base + index + 1);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static ReviewDataGenerator generator(int count, long seed, boolean includeInfra) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix("SEED");
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        properties.setIncludeInfra(includeInfra);
        return new ReviewDataGenerator(properties);
    }
}
