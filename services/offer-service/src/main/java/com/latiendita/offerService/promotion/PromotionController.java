package com.latiendita.offerService.promotion;

import com.latiendita.offerService.promotion.dto.EvaluateRequest;
import com.latiendita.offerService.promotion.dto.EvaluationDto;
import com.latiendita.offerService.promotion.dto.PromotionDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionEvaluationService evaluationService;

    @GetMapping("/active")
    public List<PromotionDto> findActive() {
        return promotionService.findActive();
    }

    @PostMapping("/evaluate")
    public EvaluationDto evaluate(@Valid @RequestBody EvaluateRequest request) {
        return evaluationService.evaluate(request);
    }
}
