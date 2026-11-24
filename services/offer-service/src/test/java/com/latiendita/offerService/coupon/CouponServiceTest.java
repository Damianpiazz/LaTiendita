package com.latiendita.offerService.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.offerService.coupon.dto.CouponDto;
import com.latiendita.offerService.exception.CouponNotFoundException;
import com.latiendita.offerService.exception.InvalidCouponException;
import com.latiendita.offerService.promotion.PromotionEntity;
import com.latiendita.offerService.promotion.PromotionRepository;
import com.latiendita.offerService.promotion.PromotionScope;
import com.latiendita.offerService.promotion.PromotionType;
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
class CouponServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private CouponRepository couponRepository;

    private CouponService service;

    @BeforeEach
    void setUp() {
        service = new CouponService(promotionRepository, couponRepository);
    }

    @Test
    void shouldValidateExistingCouponAndReportUsageCount() {
        PromotionEntity promotion = coupon("SAVE10", true, -1, +1);
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(promotion));
        when(couponRepository.countByPromotionId(promotion.getId())).thenReturn(3L);

        CouponDto result = service.validate("SAVE10");

        assertThat(result.code()).isEqualTo("SAVE10");
        assertThat(result.promotionId()).isEqualTo(promotion.getId());
        assertThat(result.valid()).isTrue();
        assertThat(result.discountValue()).isEqualByComparingTo("10.00");
        assertThat(result.minSubtotal()).isEqualByComparingTo("50.00");
        assertThat(result.usageCount()).isEqualTo(3L);
    }

    @Test
    void shouldMarkCouponInvalidWhenOutsideValidityWindow() {
        PromotionEntity expired = coupon("SAVE10", true, -5, -1);
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(expired));
        when(couponRepository.countByPromotionId(expired.getId())).thenReturn(0L);

        CouponDto result = service.validate("SAVE10");

        assertThat(result.valid()).isFalse();
    }

    @Test
    void shouldMarkCouponInvalidWhenPromotionIsInactive() {
        PromotionEntity inactive = coupon("SAVE10", false, -1, +1);
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(inactive));
        when(couponRepository.countByPromotionId(inactive.getId())).thenReturn(0L);

        CouponDto result = service.validate("SAVE10");

        assertThat(result.valid()).isFalse();
    }

    @Test
    void shouldFailWhenCouponDoesNotExist() {
        when(promotionRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validate("UNKNOWN"))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void shouldRejectPromotionWithoutCouponCode() {
        assertThatThrownBy(() -> service.validate(null))
                .isInstanceOf(InvalidCouponException.class);
    }

    @Test
    void shouldRecordCouponUsageOnce() {
        PromotionEntity promotion = coupon("SAVE10", true, -1, +1);
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(promotion));
        when(couponRepository.findByPromotionIdAndOrderId(promotion.getId(), 7L))
                .thenReturn(Optional.empty());

        service.recordUsageForCode("SAVE10", "customer-1", 7L);

        ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
        verify(couponRepository).save(captor.capture());
        assertThat(captor.getValue().getPromotionId()).isEqualTo(promotion.getId());
        assertThat(captor.getValue().getCustomerSub()).isEqualTo("customer-1");
        assertThat(captor.getValue().getOrderId()).isEqualTo(7L);
        assertThat(captor.getValue().getUsedAt()).isNotNull();
    }

    @Test
    void shouldBeIdempotentWhenUsageAlreadyExists() {
        PromotionEntity promotion = coupon("SAVE10", true, -1, +1);
        when(promotionRepository.findByCode("SAVE10")).thenReturn(Optional.of(promotion));
        when(couponRepository.findByPromotionIdAndOrderId(promotion.getId(), 7L))
                .thenReturn(Optional.of(CouponEntity.builder().orderId(7L).build()));

        service.recordUsageForCode("SAVE10", "customer-1", 7L);

        verify(couponRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreUnknownCouponCodeWithoutFailingTheConsumer() {
        when(promotionRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        service.recordUsageForCode("UNKNOWN", "customer-1", 7L);

        verify(couponRepository, never()).save(any());
    }

    private PromotionEntity coupon(String code, boolean active, int startsOffsetDays,
                                   int endsOffsetDays) {
        return PromotionEntity.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name("Save 10")
                .type(PromotionType.PERCENTAGE)
                .scope(PromotionScope.ORDER)
                .discountValue(new BigDecimal("10.00"))
                .minSubtotal(new BigDecimal("50.00"))
                .startsAt(Instant.now().plus(startsOffsetDays, ChronoUnit.DAYS))
                .endsAt(Instant.now().plus(endsOffsetDays, ChronoUnit.DAYS))
                .priority(1)
                .stackable(true)
                .active(active)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
