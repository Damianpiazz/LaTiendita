package com.latiendita.orderService.order;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>,
        JpaSpecificationExecutor<OrderEntity> {

    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    Page<OrderEntity> findByCustomerId(String customerId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findTop5ByOrderByCreatedAtDesc();

    long countByStatusIn(Collection<OrderStatus> statuses);

    List<OrderEntity> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Instant fromInclusive, Instant toExclusive);
}
