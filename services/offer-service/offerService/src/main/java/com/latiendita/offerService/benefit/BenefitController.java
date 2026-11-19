package com.latiendita.offerService.benefit;

import com.latiendita.offerService.benefit.dto.BenefitDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/benefits")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping
    public List<BenefitDto> findAll() {
        return benefitService.findAllActive();
    }
}
