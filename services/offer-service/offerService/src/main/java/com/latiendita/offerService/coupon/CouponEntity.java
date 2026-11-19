package com.latiendita.offerService.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon_usages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"promotion_id", "order_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "promotion_id", nullable = false)
    private UUID promotionId;

    @Column(name = "customer_sub", nullable = false, length = 255)
    private String customerSub;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "used_at", nullable = false)
    private Instant usedAt;
}
