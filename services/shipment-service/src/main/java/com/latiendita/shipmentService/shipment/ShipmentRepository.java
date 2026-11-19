package com.latiendita.shipmentService.shipment;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<ShipmentEntity> findByOrderId(Long orderId);

    Page<ShipmentEntity> findByStatus(ShipmentStatus status, Pageable pageable);

    long countByStatus(ShipmentStatus status);
}
