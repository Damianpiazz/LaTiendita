package com.latiendita.offerService.coupon;

import com.latiendita.offerService.coupon.dto.CouponDto;
import com.latiendita.offerService.exception.CouponNotFoundException;
import com.latiendita.offerService.exception.InvalidCouponException;
import com.latiendita.offerService.promotion.PromotionEntity;
import com.latiendita.offerService.promotion.PromotionRepository;
import com.latiendita.offerService.promotion.PromotionType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouponService {

    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public CouponDto validate(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidCouponException("Coupon code must not be blank");
        }
        PromotionEntity promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException(code));
        long usageCount = couponRepository.countByPromotionId(promotion.getId());
        boolean valid = promotion.isActive()
                && isWithinValidity(promotion)
                && promotion.getType() != PromotionType.FEATURED;
        return new CouponDto(promotion.getCode(), promotion.getId(), valid,
                promotion.getDiscountValue(), promotion.getMinSubtotal(),
                promotion.getStartsAt(), promotion.getEndsAt(), usageCount);
    }

    public void recordUsageForCode(String code, String customerSub, Long orderId) {
        if (code == null || code.isBlank()) {
            return;
        }
        Optional<PromotionEntity> promotion = promotionRepository.findByCode(code);
        if (promotion.isEmpty()) {
            log.warn("ORDER_CREATED references unknown coupon code {}; usage not recorded", code);
            return;
        }
        recordUsage(promotion.get().getId(), customerSub, orderId);
    }

    public void recordUsage(UUID promotionId, String customerSub, Long orderId) {
        if (couponRepository.findByPromotionIdAndOrderId(promotionId, orderId).isPresent()) {
            log.info("Coupon usage for promotion {} and order {} already recorded; skipping",
                    promotionId, orderId);
            return;
        }
        couponRepository.save(CouponEntity.builder()
                .promotionId(promotionId)
                .customerSub(customerSub)
                .orderId(orderId)
                .usedAt(Instant.now())
                .build());
    }

    private boolean isWithinValidity(PromotionEntity promotion) {
        Instant now = Instant.now();
        return !promotion.getStartsAt().isAfter(now)
                && (promotion.getEndsAt() == null || promotion.getEndsAt().isAfter(now));
    }
}
