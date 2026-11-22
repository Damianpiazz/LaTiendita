package com.latiendita.paymentService.payment;

import com.latiendita.paymentService.provider.ProviderType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>,
        JpaSpecificationExecutor<PaymentEntity> {

    Optional<PaymentEntity> findByOrderId(Long orderId);

    Optional<PaymentEntity> findByProviderAndProviderPaymentId(
            ProviderType provider, String providerPaymentId);

    Optional<PaymentEntity> findByExternalReference(String externalReference);

    Optional<PaymentEntity> findFirstByOrderIdAndStatusIn(
            Long orderId, Collection<PaymentStatus> statuses);

    Page<PaymentEntity> findByCustomerId(String customerId, Pageable pageable);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    long countByStatus(PaymentStatus status);

    @Query("""
            SELECT COUNT(p) FROM PaymentEntity p
            WHERE p.createdAt >= :from
            """)
    long countSince(@Param("from") Instant from);

    @Query("""
            SELECT COUNT(p) FROM PaymentEntity p
            WHERE p.status = :status AND p.createdAt >= :from
            """)
    long countByStatusSince(@Param("status") PaymentStatus status, @Param("from") Instant from);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p
            WHERE p.status = :status AND p.createdAt >= :from
            """)
    BigDecimal sumAmountByStatusSince(@Param("status") PaymentStatus status,
                                      @Param("from") Instant from);
}
