package com.latiendita.reviewService.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_votes", uniqueConstraints = @UniqueConstraint(
        name = "uk_review_votes_review_customer", columnNames = {"review_id", "customer_id"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewVoteEntity {

    @Id
    private UUID id;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
