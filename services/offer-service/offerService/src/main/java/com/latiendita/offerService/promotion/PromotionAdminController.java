package com.latiendita.offerService.promotion;

import com.latiendita.offerService.promotion.dto.CreatePromotionRequest;
import com.latiendita.offerService.promotion.dto.PromotionDto;
import com.latiendita.offerService.promotion.dto.UpdatePromotionRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class PromotionAdminController {

    private final PromotionService promotionService;

    @GetMapping
    public Page<PromotionDto> findAll(Pageable pageable) {
        return promotionService.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<PromotionDto> create(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionDto created = promotionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public PromotionDto update(@PathVariable UUID id,
                               @Valid @RequestBody UpdatePromotionRequest request) {
        return promotionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        promotionService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
