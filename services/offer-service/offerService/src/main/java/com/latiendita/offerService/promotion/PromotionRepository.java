package com.latiendita.offerService.promotion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID> {

    List<PromotionEntity> findByActiveTrue();

    Optional<PromotionEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);
}
