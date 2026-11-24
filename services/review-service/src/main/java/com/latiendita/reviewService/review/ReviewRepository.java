package com.latiendita.reviewService.review;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>,
        JpaSpecificationExecutor<ReviewEntity> {

    boolean existsByCustomerIdAndProductId(String customerId, Long productId);

    Page<ReviewEntity> findByProductIdAndStatus(Long productId, String status, Pageable pageable);

    List<ReviewEntity> findAllByProductIdAndStatus(Long productId, String status);

    List<ReviewEntity> findAllByProductId(Long productId);

    /** Reviews written by the signed-in customer, newest first. */
    List<ReviewEntity> findAllByCustomerIdOrderByCreatedAtDesc(String customerId);

    void deleteAllByProductId(Long productId);
}
