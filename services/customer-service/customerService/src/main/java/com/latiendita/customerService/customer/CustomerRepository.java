package com.latiendita.customerService.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID>,
        JpaSpecificationExecutor<CustomerEntity> {

    List<CustomerEntity> findAllByDeletedAtIsNull();

    Optional<CustomerEntity> findByAuthorizerSubAndDeletedAtIsNull(String authorizerSub);

    Optional<CustomerEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<CustomerEntity> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
