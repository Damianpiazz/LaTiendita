package com.latiendita.offerService.promotion;

import com.latiendita.offerService.exception.DuplicatePromotionCodeException;
import com.latiendita.offerService.exception.InvalidPromotionException;
import com.latiendita.offerService.exception.PromotionNotFoundException;
import com.latiendita.offerService.outbox.OutboxWriter;
import com.latiendita.offerService.promotion.dto.CreatePromotionRequest;
import com.latiendita.offerService.promotion.dto.PromotionDto;
import com.latiendita.offerService.promotion.dto.PromotionMapper;
import com.latiendita.offerService.promotion.dto.UpdatePromotionRequest;
import com.latiendita.offerService.promotion.event.PromotionChangedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    public static final String PROMOTION_EVENTS_TOPIC = "promotion-events";
    public static final String PROMOTION_CHANGED = "PROMOTION_CHANGED";

    private final PromotionRepository promotionRepository;
    private final OutboxWriter outboxWriter;

    public PromotionDto create(CreatePromotionRequest request) {
        if (hasCode(request.code()) && promotionRepository.existsByCode(request.code())) {
            throw new DuplicatePromotionCodeException(request.code());
        }
        validateWindow(request.startsAt(), request.endsAt());

        Instant now = Instant.now();
        PromotionEntity entity = PromotionEntity.builder()
                .code(blankToNull(request.code()))
                .name(request.name())
                .type(request.type())
                .scope(request.scope())
                .productId(request.productId())
                .categoryId(request.categoryId())
                .discountValue(request.discountValue())
                .minSubtotal(request.minSubtotal())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .priority(request.priority())
                .stackable(request.stackable())
                .active(request.active())
                .createdAt(now)
                .updatedAt(now)
                .build();

        PromotionEntity saved = promotionRepository.save(entity);
        publishChanged(saved);
        return PromotionMapper.toDto(saved);
    }

    public PromotionDto update(UUID id, UpdatePromotionRequest request) {
        PromotionEntity entity = findEntity(id);
        if (hasCode(request.code())
                && promotionRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicatePromotionCodeException(request.code());
        }
        validateWindow(request.startsAt(), request.endsAt());

        entity.setCode(blankToNull(request.code()));
        entity.setName(request.name());
        entity.setType(request.type());
        entity.setScope(request.scope());
        entity.setProductId(request.productId());
        entity.setCategoryId(request.categoryId());
        entity.setDiscountValue(request.discountValue());
        entity.setMinSubtotal(request.minSubtotal());
        entity.setStartsAt(request.startsAt());
        entity.setEndsAt(request.endsAt());
        entity.setPriority(request.priority());
        entity.setStackable(request.stackable());
        entity.setActive(request.active());
        entity.setUpdatedAt(Instant.now());

        PromotionEntity saved = promotionRepository.save(entity);
        publishChanged(saved);
        return PromotionMapper.toDto(saved);
    }

    public void deactivate(UUID id) {
        PromotionEntity entity = findEntity(id);
        entity.setActive(false);
        entity.setUpdatedAt(Instant.now());
        PromotionEntity saved = promotionRepository.save(entity);
        publishChanged(saved);
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> findActive() {
        Instant now = Instant.now();
        return promotionRepository.findByActiveTrue().stream()
                .filter(p -> !p.getStartsAt().isAfter(now))
                .filter(p -> p.getEndsAt() == null || p.getEndsAt().isAfter(now))
                .map(PromotionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PromotionDto> findAll(Pageable pageable) {
        return promotionRepository.findAll(pageable).map(PromotionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PromotionDto findById(UUID id) {
        return PromotionMapper.toDto(findEntity(id));
    }

    private PromotionEntity findEntity(UUID id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    private void publishChanged(PromotionEntity entity) {
        Long targetId = switch (entity.getScope()) {
            case PRODUCT -> entity.getProductId();
            case CATEGORY -> entity.getCategoryId();
            case ORDER -> null;
        };
        outboxWriter.append(PROMOTION_EVENTS_TOPIC, entity.getId().toString(), PROMOTION_CHANGED,
                new PromotionChangedEvent(PROMOTION_CHANGED, entity.getId(), entity.getScope(),
                        targetId, entity.isActive(), Instant.now()));
    }

    private void validateWindow(Instant startsAt, Instant endsAt) {
        if (startsAt == null) {
            throw new InvalidPromotionException("Promotion start date is required");
        }
        if (endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new InvalidPromotionException(
                    "Invalid promotion window: start date must be before end date");
        }
    }

    private boolean hasCode(String code) {
        return code != null && !code.isBlank();
    }

    private String blankToNull(String code) {
        return hasCode(code) ? code.trim() : null;
    }
}
