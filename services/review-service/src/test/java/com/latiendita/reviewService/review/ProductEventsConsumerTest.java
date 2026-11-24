package com.latiendita.reviewService.review;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductEventsConsumerTest {

    private static final String DELETED = """
            {"type":"PRODUCT_DELETED","productId":42,"occurredAt":"2026-11-19T10:01:00Z"}
            """;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewVoteRepository reviewVoteRepository;

    @Mock
    private ProductRatingRepository productRatingRepository;

    private ProductEventsConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ProductEventsConsumer(reviewRepository, reviewVoteRepository,
                productRatingRepository, new ObjectMapper());
    }

    @Test
    void shouldRemoveAllReviewDataForDeletedProduct() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findAllByProductId(42L))
                .thenReturn(List.of(ReviewEntity.builder().id(reviewId).build()));

        consumer.onProductEvent(DELETED);

        verify(reviewVoteRepository).deleteAllByReviewIdIn(List.of(reviewId));
        verify(reviewRepository).deleteAllByProductId(42L);
        verify(productRatingRepository).deleteById(42L);
    }

    @Test
    void shouldIgnoreUnrelatedEventTypes() {
        consumer.onProductEvent("""
                {"type":"PRODUCT_PRICE_CHANGED","productId":42,"price":12.50}
                """);
        consumer.onProductEvent("""
                {"type":"ORDER_CREATED","productId":42}
                """);

        verifyNoInteractions(reviewRepository, reviewVoteRepository, productRatingRepository);
    }

    @Test
    void shouldDiscardMalformedPayloads() {
        consumer.onProductEvent("esto no es json {{{");
        consumer.onProductEvent("");
        consumer.onProductEvent("""
                {"type":"PRODUCT_DELETED"}
                """);

        verify(reviewRepository, never()).deleteAllByProductId(ArgumentMatchers.anyLong());
        verify(reviewVoteRepository, never())
                .deleteAllByReviewIdIn(ArgumentMatchers.<List<UUID>>any());
        verify(productRatingRepository, never()).deleteById(ArgumentMatchers.anyLong());
    }
}
