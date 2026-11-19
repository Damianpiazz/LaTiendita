package com.latiendita.shipmentService.address;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddressEntity, Long> {

    List<DeliveryAddressEntity> findByCustomerIdOrderByIdDesc(String customerId);
}
