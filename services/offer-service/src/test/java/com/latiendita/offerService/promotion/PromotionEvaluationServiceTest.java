package com.latiendita.offerService.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.latiendita.offerService.promotion.dto.EvaluateRequest;
import com.latiendita.offerService.promotion.dto.EvaluationDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionEvaluationServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    private PromotionEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new PromotionEvaluationService(promotionRepository);
    }

    @Test
    void shouldPickHighestPriorityPromotionForTheTarget() {
        PromotionEntity low = promotion("LOW", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                1L, null, "25", "5", 1, true);
        PromotionEntity high = promotion("HIGH", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                1L, null, "10", "5", 9, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(low, high));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.promotionId()).isEqualTo(high.getId());
        assertThat(result.appliedAmount()).isEqualByComparingTo("20.00");
        assertThat(result.eligible()).singleElement().satisfies(target -> {
            assertThat(target.targetId()).isEqualTo(1L);
            assertThat(target.discountValue()).isEqualByComparingTo("20.00");
        });
    }

    @Test
    void shouldIgnorePromotionsBeforeStartOrAfterEnd() {
        PromotionEntity future = promotion("FUTURE", PromotionType.PERCENTAGE, PromotionScope.ORDER,
                null, null, "10", null, 5, true);
        future.setStartsAt(Instant.now().plus(1, ChronoUnit.DAYS));

        PromotionEntity expired = promotion("EXPIRED", PromotionType.PERCENTAGE, PromotionScope.ORDER,
                null, null, "10", null, 5, true);
        expired.setStartsAt(Instant.now().minus(2, ChronoUnit.DAYS));
        expired.setEndsAt(Instant.now().minus(1, ChronoUnit.DAYS));

        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(future, expired));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.appliedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.eligible()).isEmpty();
        assertThat(result.promotionId()).isNull();
    }

    @Test
    void shouldIncludePromotionEndingInTheFuture() {
        PromotionEntity active = promotion("ACTIVE", PromotionType.PERCENTAGE, PromotionScope.ORDER,
                null, null, "10", null, 5, true);
        active.setEndsAt(Instant.now().plus(1, ChronoUnit.DAYS));
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(active));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.appliedAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldIgnorePromotionBelowMinimumSubtotal() {
        PromotionEntity minimum = promotion("MIN", PromotionType.FIXED_AMOUNT, PromotionScope.ORDER,
                null, null, "50", "500", 5, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(minimum));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.appliedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.eligible()).isEmpty();
    }

    @Test
    void shouldApplyPromotionAtExactlyTheMinimumSubtotal() {
        PromotionEntity minimum = promotion("MIN", PromotionType.FIXED_AMOUNT, PromotionScope.ORDER,
                null, null, "50", "200", 5, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(minimum));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.appliedAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldMatchCategoryScopeThroughCartItems() {
        PromotionEntity category = promotion("CAT", PromotionType.PERCENTAGE, PromotionScope.CATEGORY,
                null, 10L, "15", null, 5, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(category));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.promotionId()).isEqualTo(category.getId());
        assertThat(result.eligible()).singleElement()
                .extracting(EvaluationDto.EligibleTarget::targetId).isEqualTo(10L);
    }

    @Test
    void shouldIgnoreScopeThatDoesNotMatchAnyCartItem() {
        PromotionEntity otherCategory = promotion("OTHER", PromotionType.PERCENTAGE,
                PromotionScope.CATEGORY, null, 99L, "15", null, 5, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(otherCategory));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.eligible()).isEmpty();
    }

    @Test
    void shouldPreferMostRecentStartWhenPriorityTies() {
        PromotionEntity older = promotion("OLDER", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                1L, null, "10", null, 5, true);
        older.setStartsAt(Instant.now().minus(3, ChronoUnit.DAYS));
        PromotionEntity newer = promotion("NEWER", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                1L, null, "20", null, 5, true);
        newer.setStartsAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(older, newer));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.promotionId()).isEqualTo(newer.getId());
        assertThat(result.appliedAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void shouldCombineStackablePromotionsOnDifferentTargets() {
        PromotionEntity product = promotion("PRODUCT", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                1L, null, "10", null, 5, true);
        PromotionEntity category = promotion("CATEGORY", PromotionType.PERCENTAGE,
                PromotionScope.CATEGORY, null, 10L, "5", null, 4, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(product, category));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.eligible()).hasSize(2);
        assertThat(result.appliedAmount()).isEqualByComparingTo("30.00");
        assertThat(result.promotionId()).isEqualTo(product.getId());
    }

    @Test
    void shouldExcludeCompetingNonStackablePromotions() {
        PromotionEntity first = promotion("FIRST", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                1L, null, "10", null, 5, false);
        PromotionEntity second = promotion("SECOND", PromotionType.PERCENTAGE,
                PromotionScope.CATEGORY, null, 10L, "20", null, 4, false);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(first, second));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.eligible()).hasSize(1);
        assertThat(result.promotionId()).isEqualTo(first.getId());
        assertThat(result.appliedAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldCapAppliedAmountAtSubtotal() {
        PromotionEntity huge = promotion("HUGE", PromotionType.FIXED_AMOUNT, PromotionScope.ORDER,
                null, null, "500", null, 5, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(huge));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.appliedAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldTreatFeaturedPromotionAsNonDiscounting() {
        PromotionEntity featured = promotion("FEATURED", PromotionType.FEATURED, PromotionScope.ORDER,
                null, null, null, null, 5, true);
        when(promotionRepository.findByActiveTrue()).thenReturn(List.of(featured));

        EvaluationDto result = service.evaluate(request(200));

        assertThat(result.promotionId()).isEqualTo(featured.getId());
        assertThat(result.type()).isEqualTo(PromotionType.FEATURED);
        assertThat(result.appliedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private EvaluateRequest request(int subtotal) {
        return new EvaluateRequest(
                List.of(new EvaluateRequest.Item(1L, 10L, 2, new BigDecimal("100.00"))),
                new BigDecimal(subtotal));
    }

    private PromotionEntity promotion(String code, PromotionType type, PromotionScope scope,
                                      Long productId, Long categoryId, String discount,
                                      String minSubtotal, int priority, boolean stackable) {
        return PromotionEntity.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(code)
                .type(type)
                .scope(scope)
                .productId(productId)
                .categoryId(categoryId)
                .discountValue(discount == null ? null : new BigDecimal(discount))
                .minSubtotal(minSubtotal == null ? null : new BigDecimal(minSubtotal))
                .startsAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .endsAt(null)
                .priority(priority)
                .stackable(stackable)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
