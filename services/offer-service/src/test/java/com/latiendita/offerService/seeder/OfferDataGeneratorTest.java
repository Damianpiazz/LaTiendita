package com.latiendita.offerService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.offerService.seeder.model.SeedDataset;
import com.latiendita.offerService.seeder.model.SeededCouponUsage;
import com.latiendita.offerService.seeder.model.SeededOutboxEvent;
import com.latiendita.offerService.seeder.model.SeededPromotion;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class OfferDataGeneratorTest {

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(25, 42, true).generate())
                .isEqualTo(generator(25, 42, true).generate());
    }

    @Test
    void shouldChangeDiscountsWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(25, 42, true).generate();
        SeedDataset second = generator(25, 43, true).generate();

        assertThat(second.promotions().get(1).discountValue())
                .isNotEqualTo(first.promotions().get(1).discountValue());
        assertThat(second.promotions().get(0).id()).isEqualTo(first.promotions().get(0).id());
        assertThat(second.promotions().get(0).code()).isEqualTo(first.promotions().get(0).code());
    }

    @Test
    void shouldRespectVolumeEnumsAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, true).generate();

        assertThat(dataset.promotions()).hasSize(1000);
        assertThat(dataset.usages()).hasSize(1000);
        assertThat(dataset.benefits()).hasSize(20);
        assertThat(dataset.events()).hasSize(1000);

        assertThat(unique(dataset.promotions().stream().map(SeededPromotion::id))).hasSize(1000);
        assertThat(unique(dataset.promotions().stream().map(SeededPromotion::code))).hasSize(1000);
        assertThat(unique(dataset.usages().stream().map(SeededCouponUsage::id))).hasSize(1000);
        assertThat(unique(dataset.usages().stream().map(SeededCouponUsage::orderId))).hasSize(1000);
        assertThat(unique(dataset.events().stream().map(SeededOutboxEvent::aggregateId)))
                .hasSize(1000);

        assertThat(countType(dataset, "FEATURED")).isEqualTo(334);
        assertThat(countType(dataset, "PERCENTAGE")).isEqualTo(333);
        assertThat(countType(dataset, "FIXED_AMOUNT")).isEqualTo(333);
        assertThat(dataset.promotions()).allSatisfy(promotion -> {
            assertThat(promotion.type()).isIn("FEATURED", "PERCENTAGE", "FIXED_AMOUNT");
            assertThat(promotion.scope()).isIn("PRODUCT", "CATEGORY", "ORDER");
            assertThat(promotion.endsAt()).isAfter(promotion.startsAt());
            assertThat(promotion.updatedAt()).isAfter(promotion.createdAt());
            assertThat(promotion.priority()).isBetween(1, 10);
            switch (promotion.scope()) {
                case "PRODUCT" -> {
                    assertThat(promotion.productId()).isBetween(9_100_000L, 9_100_999L);
                    assertThat(promotion.categoryId()).isNull();
                }
                case "CATEGORY" -> {
                    assertThat(promotion.categoryId()).isBetween(20L, 39L);
                    assertThat(promotion.productId()).isNull();
                }
                default -> {
                    assertThat(promotion.productId()).isNull();
                    assertThat(promotion.categoryId()).isNull();
                }
            }
            if (promotion.type().equals("FEATURED")) {
                assertThat(promotion.discountValue()).isNull();
            } else {
                assertThat(promotion.discountValue()).isPositive();
            }
        });

        Set<UUID> promotionIds = dataset.promotions().stream()
                .map(SeededPromotion::id).collect(Collectors.toSet());
        Set<UUID> nonFeaturedIds = dataset.promotions().stream()
                .filter(p -> !p.type().equals("FEATURED"))
                .map(SeededPromotion::id).collect(Collectors.toSet());
        assertThat(dataset.usages()).allSatisfy(usage -> {
            assertThat(promotionIds).contains(usage.promotionId());
            assertThat(nonFeaturedIds).contains(usage.promotionId());
            assertThat(usage.customerSub()).startsWith("SEED-CUST-");
            assertThat(usage.orderId()).isBetween(9_200_000L, 9_200_999L);
        });

        assertThat(dataset.benefits()).allSatisfy(benefit -> {
            assertThat(benefit.code()).startsWith("SEED-BENEFIT-");
            assertThat(benefit.sortOrder()).isBetween(1, 20);
            assertThat(benefit.active()).isTrue();
        });

        assertThat(dataset.events()).allSatisfy(event -> {
            assertThat(event.topic()).isEqualTo("promotion-events");
            assertThat(event.eventType()).isEqualTo("PROMOTION_CHANGED");
            assertThat(event.status()).isEqualTo("SENT");
            assertThat(event.attempts()).isEqualTo(1);
            assertThat(event.sentAt()).isAfter(event.createdAt());
            assertThat(event.payload()).contains("\"source\":\"SEED\"")
                    .contains("PROMOTION_CHANGED");
            assertThat(promotionIds).contains(UUID.fromString(event.aggregateId()));
        });
    }

    @Test
    void shouldDeriveCrossServiceIdsFromPrefixAndBase() {
        SeedDataset dataset = generator(5, 42, true).generate();
        UUID expectedPromotion = UUID.nameUUIDFromBytes(
                "SEED-PROMO-9000001".getBytes(StandardCharsets.UTF_8));

        assertThat(dataset.promotions().get(0).id()).isEqualTo(expectedPromotion);
        assertThat(dataset.promotions().get(0).code()).isEqualTo("SEED-PROMO-000001");
        assertThat(dataset.usages().get(0).customerSub()).isEqualTo("SEED-CUST-9000001");
        assertThat(dataset.usages().get(0).orderId()).isEqualTo(9_200_000L);
        assertThat(dataset.benefits().get(0).code()).isEqualTo("SEED-BENEFIT-000001");
        assertThat(generator(5, 42, true).generate().promotions().get(0).id())
                .isEqualTo(expectedPromotion);
        assertThat(generator(5, 42, false).generate().events()).isEmpty();
    }

    private static long countType(SeedDataset dataset, String type) {
        return dataset.promotions().stream().filter(p -> p.type().equals(type)).count();
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static OfferDataGenerator generator(int count, long seed, boolean includeInfra) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix("SEED");
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        properties.setIncludeInfra(includeInfra);
        return new OfferDataGenerator(properties);
    }
}
