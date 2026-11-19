package com.latiendita.reviewService.review;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_ratings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRatingEntity {

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal average;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Convert(converter = JsonDistributionConverter.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Integer> distribution;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ProductRatingEntity from(long productId, List<ReviewEntity> approved) {
        Map<String, Integer> distribution = JsonDistributionConverter.emptyDistribution();
        BigDecimal sum = BigDecimal.ZERO;
        for (ReviewEntity review : approved) {
            String star = String.valueOf(review.getRating());
            distribution.merge(star, 1, Integer::sum);
            sum = sum.add(BigDecimal.valueOf(review.getRating()));
        }
        BigDecimal average = approved.isEmpty() ? BigDecimal.ZERO.setScale(2)
                : sum.divide(BigDecimal.valueOf(approved.size()), 2, RoundingMode.HALF_UP);
        return ProductRatingEntity.builder()
                .productId(productId)
                .average(average)
                .reviewCount(approved.size())
                .distribution(distribution)
                .updatedAt(Instant.now())
                .build();
    }

    public static ProductRatingEntity empty(long productId) {
        return ProductRatingEntity.builder()
                .productId(productId)
                .average(BigDecimal.ZERO.setScale(2))
                .reviewCount(0)
                .distribution(new LinkedHashMap<>(JsonDistributionConverter.emptyDistribution()))
                .updatedAt(Instant.now())
                .build();
    }
}
