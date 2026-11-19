package com.latiendita.reviewService.review;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewVoteRepository extends JpaRepository<ReviewVoteEntity, UUID> {

    boolean existsByReviewIdAndCustomerId(UUID reviewId, String customerId);

    void deleteAllByReviewIdIn(List<UUID> reviewIds);
}
