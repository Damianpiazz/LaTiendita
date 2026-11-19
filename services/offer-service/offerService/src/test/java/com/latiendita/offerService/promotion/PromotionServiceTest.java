package com.latiendita.offerService.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.offerService.exception.DuplicatePromotionCodeException;
import com.latiendita.offerService.exception.InvalidPromotionException;
import com.latiendita.offerService.exception.PromotionNotFoundException;
import com.latiendita.offerService.outbox.OutboxWriter;
import com.latiendita.offerService.promotion.dto.CreatePromotionRequest;
import com.latiendita.offerService.promotion.dto.UpdatePromotionRequest;
import com.latiendita.offerService.promotion.event.PromotionChangedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private OutboxWriter outboxWriter;

    private PromotionService service;

    @BeforeEach
    void setUp() {
        service = new PromotionService(promotionRepository, outboxWriter);
    }

    @Test
    void shouldCreatePromotionAndPublishPromotionChanged() {
        when(promotionRepository.existsByCode("SAVE10")).thenReturn(false);
        when(promotionRepository.save(any(PromotionEntity.class))).thenAnswer(invocation -> {
            PromotionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        UUID id = service.create(createRequest("SAVE10")).id();

        verify(outboxWriter).append(eq("promotion-events"), eq(id.toString()),
                eq("PROMOTION_CHANGED"), any(PromotionChangedEvent.class));
        ArgumentCaptor<PromotionChangedEvent> captor =
                ArgumentCaptor.forClass(PromotionChangedEvent.class);
        verify(outboxWriter).append(eq("promotion-events"), eq(id.toString()),
                eq("PROMOTION_CHANGED"), captor.capture());
        PromotionChangedEvent event = captor.getValue();
        assertThat(event.type()).isEqualTo("PROMOTION_CHANGED");
        assertThat(event.promotionId()).isEqualTo(id);
        assertThat(event.scope()).isEqualTo(PromotionScope.ORDER);
        assertThat(event.targetId()).isNull();
        assertThat(event.active()).isTrue();
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void shouldPublishTargetIdForProductScopedPromotionOnUpdate() {
        PromotionEntity existing = existing();
        when(promotionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(promotionRepository.existsByCodeAndIdNot("SAVE10", existing.getId())).thenReturn(false);
        when(promotionRepository.save(existing)).thenReturn(existing);

        service.update(existing.getId(), updateRequest(11L));

        ArgumentCaptor<PromotionChangedEvent> captor =
                ArgumentCaptor.forClass(PromotionChangedEvent.class);
        verify(outboxWriter).append(eq("promotion-events"), eq(existing.getId().toString()),
                eq("PROMOTION_CHANGED"), captor.capture());
        assertThat(captor.getValue().scope()).isEqualTo(PromotionScope.PRODUCT);
        assertThat(captor.getValue().targetId()).isEqualTo(11L);
    }

    @Test
    void shouldDeactivatePromotionAndPublishInactiveEvent() {
        PromotionEntity existing = existing();
        when(promotionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(promotionRepository.save(existing)).thenReturn(existing);

        service.deactivate(existing.getId());

        assertThat(existing.isActive()).isFalse();
        ArgumentCaptor<PromotionChangedEvent> captor =
                ArgumentCaptor.forClass(PromotionChangedEvent.class);
        verify(outboxWriter).append(eq("promotion-events"), eq(existing.getId().toString()),
                eq("PROMOTION_CHANGED"), captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void shouldRejectDuplicateCodeWithoutWritingOutbox() {
        when(promotionRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest("SAVE10")))
                .isInstanceOf(DuplicatePromotionCodeException.class)
                .hasMessageContaining("SAVE10");

        verify(promotionRepository, never()).save(any());
        verify(outboxWriter, never()).append(any(), any(), any(), any());
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        CreatePromotionRequest request = new CreatePromotionRequest(
                "SAVE10", "Save 10", PromotionType.PERCENTAGE, PromotionScope.ORDER,
                null, null, new BigDecimal("10"), null,
                Instant.now(), Instant.now().minus(1, ChronoUnit.DAYS),
                1, true, true);
        when(promotionRepository.existsByCode("SAVE10")).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidPromotionException.class)
                .hasMessageContaining("start");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenPromotionDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(promotionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(id))
                .isInstanceOf(PromotionNotFoundException.class);
    }

    private PromotionEntity existing() {
        return PromotionEntity.builder()
                .id(UUID.randomUUID())
                .code("SAVE10")
                .name("Save 10")
                .type(PromotionType.PERCENTAGE)
                .scope(PromotionScope.ORDER)
                .discountValue(new BigDecimal("10"))
                .startsAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .priority(1)
                .stackable(true)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private CreatePromotionRequest createRequest(String code) {
        return new CreatePromotionRequest(
                code, "Save 10", PromotionType.PERCENTAGE, PromotionScope.ORDER,
                null, null, new BigDecimal("10"), new BigDecimal("50"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS),
                1, true, true);
    }

    private UpdatePromotionRequest updateRequest(Long productId) {
        return new UpdatePromotionRequest(
                "SAVE10", "Save 10", PromotionType.PERCENTAGE, PromotionScope.PRODUCT,
                productId, null, new BigDecimal("10"), new BigDecimal("50"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS),
                1, true, true);
    }
}
