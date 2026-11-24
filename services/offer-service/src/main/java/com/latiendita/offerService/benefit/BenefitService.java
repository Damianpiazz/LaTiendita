package com.latiendita.offerService.benefit;

import com.latiendita.offerService.benefit.dto.BenefitDto;
import com.latiendita.offerService.benefit.dto.BenefitMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BenefitService {

    private final BenefitRepository benefitRepository;

    public List<BenefitDto> findAllActive() {
        return benefitRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(BenefitMapper::toDto)
                .toList();
    }
}
