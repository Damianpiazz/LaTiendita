package com.latiendita.offerService.coupon;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findByPromotionIdAndOrderId(UUID promotionId, Long orderId);

    long countByPromotionId(UUID promotionId);
}
