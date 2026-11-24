package com.latiendita.offerService.promotion;

import com.latiendita.offerService.promotion.dto.EvaluateRequest;
import com.latiendita.offerService.promotion.dto.EvaluationDto;
import com.latiendita.offerService.promotion.dto.EvaluationDto.EligibleTarget;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionEvaluationService {

    private final PromotionRepository promotionRepository;

    public EvaluationDto evaluate(EvaluateRequest request) {
        BigDecimal subtotal = request.subtotal() == null ? BigDecimal.ZERO : request.subtotal();
        List<EvaluateRequest.Item> items = request.items() == null ? List.of() : request.items();
        Instant now = Instant.now();

        List<PromotionEntity> candidates = promotionRepository.findByActiveTrue().stream()
                .filter(p -> p.isActive())
                .filter(p -> isValidAt(p, now))
                .filter(p -> matchesScope(p, items))
                .filter(p -> meetsMinimumSubtotal(p, subtotal))
                .sorted(BY_PRIORITY_THEN_MOST_RECENT_START)
                .toList();

        Map<Long, PromotionEntity> bestPerTarget = new LinkedHashMap<>();
        for (PromotionEntity promotion : candidates) {
            bestPerTarget.putIfAbsent(targetId(promotion), promotion);
        }

        List<PromotionEntity> selected = selectStackable(bestPerTarget.values());
        if (selected.isEmpty()) {
            return new EvaluationDto(null, null, null, BigDecimal.ZERO, List.of());
        }

        List<EligibleTarget> eligible = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PromotionEntity promotion : selected) {
            BigDecimal discount = discountFor(promotion, subtotal);
            eligible.add(new EligibleTarget(targetId(promotion), discount));
            total = total.add(discount);
        }

        PromotionEntity winner = selected.get(0);
        return new EvaluationDto(winner.getId(), winner.getType(), winner.getDiscountValue(),
                total.min(subtotal), eligible);
    }

    private static final Comparator<PromotionEntity> BY_PRIORITY_THEN_MOST_RECENT_START =
            Comparator.comparingInt(PromotionEntity::getPriority).reversed()
                    .thenComparing(PromotionEntity::getStartsAt,
                            Comparator.nullsLast(Comparator.reverseOrder()));

    /**
     * Non-stackable promotions are mutually exclusive and also block any other
     * promotion from combining with them. Candidates arrive sorted by priority.
     */
    private List<PromotionEntity> selectStackable(java.util.Collection<PromotionEntity> candidates) {
        List<PromotionEntity> selected = new ArrayList<>();
        for (PromotionEntity promotion : candidates) {
            boolean blockedBySelected = selected.stream().anyMatch(p -> !p.isStackable());
            if (blockedBySelected) {
                break;
            }
            if (!promotion.isStackable() && !selected.isEmpty()) {
                continue;
            }
            selected.add(promotion);
        }
        return selected;
    }

    private boolean isValidAt(PromotionEntity promotion, Instant now) {
        return !promotion.getStartsAt().isAfter(now)
                && (promotion.getEndsAt() == null || promotion.getEndsAt().isAfter(now));
    }

    private boolean matchesScope(PromotionEntity promotion, List<EvaluateRequest.Item> items) {
        return switch (promotion.getScope()) {
            case ORDER -> true;
            case PRODUCT -> promotion.getProductId() != null && items.stream()
                    .anyMatch(item -> promotion.getProductId().equals(item.productId()));
            case CATEGORY -> promotion.getCategoryId() != null && items.stream()
                    .anyMatch(item -> promotion.getCategoryId().equals(item.categoryId()));
        };
    }

    private boolean meetsMinimumSubtotal(PromotionEntity promotion, BigDecimal subtotal) {
        return promotion.getMinSubtotal() == null
                || subtotal.compareTo(promotion.getMinSubtotal()) >= 0;
    }

    private Long targetId(PromotionEntity promotion) {
        return switch (promotion.getScope()) {
            case PRODUCT -> promotion.getProductId();
            case CATEGORY -> promotion.getCategoryId();
            case ORDER -> null;
        };
    }

    private BigDecimal discountFor(PromotionEntity promotion, BigDecimal subtotal) {
        if (promotion.getType() != PromotionType.PERCENTAGE
                && promotion.getType() != PromotionType.FIXED_AMOUNT) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = promotion.getDiscountValue();
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return promotion.getType() == PromotionType.PERCENTAGE
                ? subtotal.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : value;
    }
}
