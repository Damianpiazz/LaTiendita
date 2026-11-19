package com.latiendita.offerService.benefit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitRepository extends JpaRepository<BenefitEntity, UUID> {

    List<BenefitEntity> findByActiveTrueOrderBySortOrderAsc();
}
