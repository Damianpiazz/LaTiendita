package com.latiendita.cartService.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductEventsConsumerTest {

    private static final String PRICE_CHANGED = """
            {"type":"PRODUCT_PRICE_CHANGED","productId":42,"price":12.50,\
            "occurredAt":"2026-11-19T10:00:00Z"}
            """;

    private static final String DELETED = """
            {"type":"PRODUCT_DELETED","productId":42,"occurredAt":"2026-11-19T10:01:00Z"}
            """;

    @Mock
    private CartItemRepository cartItemRepository;

    private ProductEventsConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ProductEventsConsumer(cartItemRepository, new ObjectMapper());
    }

    @Test
    void shouldUpdatePriceSnapshotAndFlagChange() {
        CartItemEntity item = item(new BigDecimal("10.00"), false);
        when(cartItemRepository.findAllByProductId(42L)).thenReturn(List.of(item));

        consumer.onProductEvent(PRICE_CHANGED);

        assertThat(item.getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(item.isPriceChanged()).isTrue();
        verify(cartItemRepository).save(item);
    }

    @Test
    void shouldBeIdempotentWhenPriceAlreadyApplied() {
        CartItemEntity item = item(new BigDecimal("12.50"), true);
        when(cartItemRepository.findAllByProductId(42L)).thenReturn(List.of(item));

        consumer.onProductEvent(PRICE_CHANGED);
        consumer.onProductEvent(PRICE_CHANGED);

        assertThat(item.getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(item.isPriceChanged()).isTrue();
        verify(cartItemRepository, never()).save(item);
    }

    @Test
    void shouldRemoveAllCartItemsForDeletedProduct() {
        List<CartItemEntity> items = List.of(item(new BigDecimal("10.00"), false));
        when(cartItemRepository.findAllByProductId(42L)).thenReturn(items);

        consumer.onProductEvent(DELETED);

        verify(cartItemRepository).deleteAll(items);
    }

    @Test
    void shouldBeIdempotentWhenDeletedProductIsAlreadyGone() {
        when(cartItemRepository.findAllByProductId(42L)).thenReturn(List.of());

        consumer.onProductEvent(DELETED);
        consumer.onProductEvent(DELETED);

        verify(cartItemRepository, never()).deleteAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void shouldIgnoreUnrelatedEventTypes() {
        consumer.onProductEvent("""
                {"type":"PRODUCT_CREATED","productId":42}
                """);
        consumer.onProductEvent("""
                {"type":"ORDER_CREATED","productId":42}
                """);

        verify(cartItemRepository, never()).findAllByProductId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void shouldDiscardMalformedPayloads() {
        consumer.onProductEvent("esto no es json {{{");
        consumer.onProductEvent("");
        consumer.onProductEvent("""
                {"type":"PRODUCT_PRICE_CHANGED","productId":42}
                """);
        consumer.onProductEvent("""
                {"type":"PRODUCT_DELETED"}
                """);

        verify(cartItemRepository, never()).findAllByProductId(org.mockito.ArgumentMatchers.anyLong());
    }

    private CartItemEntity item(BigDecimal price, boolean priceChanged) {
        return CartItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(42L)
                .quantity(1)
                .unitPrice(price)
                .priceChanged(priceChanged)
                .createdAt(Instant.parse("2026-11-19T09:00:00Z"))
                .updatedAt(Instant.parse("2026-11-19T09:00:00Z"))
                .build();
    }
}
