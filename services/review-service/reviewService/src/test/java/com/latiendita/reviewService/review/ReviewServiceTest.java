package com.latiendita.reviewService.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.latiendita.reviewService.CurrentUser;
import com.latiendita.reviewService.exception.AdminRoleRequiredException;
import com.latiendita.reviewService.exception.DuplicateReviewException;
import com.latiendita.reviewService.exception.DuplicateVoteException;
import com.latiendita.reviewService.exception.InvalidRatingException;
import com.latiendita.reviewService.exception.InvalidReviewStatusException;
import com.latiendita.reviewService.exception.NotPurchasedException;
import com.latiendita.reviewService.exception.OrderNotEligibleException;
import com.latiendita.reviewService.exception.OrderServiceUnavailableException;
import com.latiendita.reviewService.exception.ReviewNotVotableException;
import com.latiendita.reviewService.exception.ReviewNotFoundException;
import com.latiendita.reviewService.exception.SelfVoteException;
import com.latiendita.reviewService.order.OrderClient;
import com.latiendita.reviewService.order.OrderDto;
import com.latiendita.reviewService.outbox.OutboxWriter;
import com.latiendita.reviewService.product.ProductClient;
import com.latiendita.reviewService.review.dto.CreateReviewRequest;
import com.latiendita.reviewService.review.dto.ModerateReviewRequest;
import com.latiendita.reviewService.review.dto.ReviewSummaryDto;
import com.latiendita.reviewService.review.event.ReviewApprovedEvent;
import com.latiendita.reviewService.review.event.ReviewStatusChangedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final String CUSTOMER = "auth0|customer-1";
    private static final String OTHER_CUSTOMER = "auth0|customer-2";
    private static final long PRODUCT_ID = 42L;
    private static final long ORDER_ID = 9L;
    private static final UUID REVIEW_ID = UUID.fromString("3f2504e0-4f89-11d3-9a0c-0305e82c3301");
    private static final String REVIEW_EVENTS = "review-events";

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewVoteRepository reviewVoteRepository;

    @Mock
    private ProductRatingRepository productRatingRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private OutboxWriter outboxWriter;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        setGatewayHeaders(CUSTOMER, "cliente");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldCreatePendingReviewWhenCustomerPurchasedProduct() {
        when(orderClient.findPurchases()).thenReturn(List.of(paidOrder()));
        when(reviewRepository.existsByCustomerIdAndProductId(CUSTOMER, PRODUCT_ID)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> {
            ReviewEntity entity = invocation.getArgument(0);
            entity.setId(REVIEW_ID);
            return entity;
        });

        var created = reviewService.create(new CreateReviewRequest(PRODUCT_ID, 5, "Excelente", "Muy bueno"));

        assertThat(created.id()).isEqualTo(REVIEW_ID);
        assertThat(created.customerId()).isEqualTo(CUSTOMER);
        assertThat(created.orderId()).isEqualTo(ORDER_ID);
        assertThat(created.rating()).isEqualTo(5);
        assertThat(created.status()).isEqualTo(ReviewStatus.PENDING);
        verify(productClient).requireExisting(PRODUCT_ID);
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void shouldRejectReviewWhenCustomerNeverPurchasedProduct() {
        when(orderClient.findPurchases()).thenReturn(List.of());

        assertThatThrownBy(() -> reviewService.create(
                new CreateReviewRequest(PRODUCT_ID, 5, "Excelente", "Muy bueno")))
                .isInstanceOf(NotPurchasedException.class)
                .hasMessageContaining("Only customers who purchased");
        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void shouldRejectReviewWhenOnlyOrderIsCancelled() {
        when(orderClient.findPurchases()).thenReturn(List.of(order("CANCELLED", PRODUCT_ID)));

        assertThatThrownBy(() -> reviewService.create(
                new CreateReviewRequest(PRODUCT_ID, 5, "Excelente", "Muy bueno")))
                .isInstanceOf(OrderNotEligibleException.class)
                .hasMessageContaining("CANCELLED");
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void shouldRejectDuplicateReviewForSameCustomerAndProduct() {
        when(orderClient.findPurchases()).thenReturn(List.of(paidOrder()));
        when(reviewRepository.existsByCustomerIdAndProductId(CUSTOMER, PRODUCT_ID)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(
                new CreateReviewRequest(PRODUCT_ID, 5, "Excelente", "Muy bueno")))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessageContaining("already reviewed product");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldPropagateOrderServiceUnavailability() {
        when(orderClient.findPurchases()).thenThrow(new OrderServiceUnavailableException());

        assertThatThrownBy(() -> reviewService.create(
                new CreateReviewRequest(PRODUCT_ID, 5, "Excelente", "Muy bueno")))
                .isInstanceOf(OrderServiceUnavailableException.class);
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void shouldRejectRatingOutsideOneToFive() {
        assertThatThrownBy(() -> reviewService.create(
                new CreateReviewRequest(PRODUCT_ID, 6, "Excelente", "Muy bueno")))
                .isInstanceOf(InvalidRatingException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
        verifyNoInteractions(orderClient, productClient, outboxWriter);
    }

    @Test
    void shouldAllowHelpfulVoteOnceAndIncrementCounter() {
        setGatewayHeaders(OTHER_CUSTOMER, "cliente");
        ReviewEntity review = review(ReviewStatus.APPROVED, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewVoteRepository.existsByReviewIdAndCustomerId(REVIEW_ID, OTHER_CUSTOMER)).thenReturn(false);
        when(reviewRepository.save(review)).thenReturn(review);

        var result = reviewService.markHelpful(REVIEW_ID);

        assertThat(result.helpfulVotes()).isEqualTo(1);
        verify(reviewVoteRepository).save(any());
        verify(reviewRepository).save(review);
    }

    @Test
    void shouldRejectHelpfulVoteOnOwnReview() {
        ReviewEntity review = review(ReviewStatus.APPROVED, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.markHelpful(REVIEW_ID))
                .isInstanceOf(SelfVoteException.class);
        verify(reviewVoteRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateHelpfulVote() {
        setGatewayHeaders(OTHER_CUSTOMER, "cliente");
        ReviewEntity review = review(ReviewStatus.APPROVED, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewVoteRepository.existsByReviewIdAndCustomerId(REVIEW_ID, OTHER_CUSTOMER)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.markHelpful(REVIEW_ID))
                .isInstanceOf(DuplicateVoteException.class);
        verify(reviewVoteRepository, never()).save(any());
    }

    @Test
    void shouldRejectHelpfulVoteOnPendingReview() {
        ReviewEntity review = review(ReviewStatus.PENDING, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.markHelpful(REVIEW_ID))
                .isInstanceOf(ReviewNotVotableException.class);
    }

    @Test
    void shouldFailWhenReviewDoesNotExist() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.markHelpful(REVIEW_ID))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("Review with id");
    }

    @Test
    void shouldReturnZeroedSummaryWhenProductHasNoRating() {
        when(productRatingRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        ReviewSummaryDto summary = reviewService.summary(PRODUCT_ID);

        assertThat(summary.productId()).isEqualTo(PRODUCT_ID);
        assertThat(summary.average()).isEqualByComparingTo("0.00");
        assertThat(summary.reviewCount()).isZero();
        assertThat(summary.distribution()).containsOnlyKeys("1", "2", "3", "4", "5");
        assertThat(summary.distribution().values()).allMatch(count -> count == 0);
    }

    @Test
    void shouldListOnlyApprovedReviews() {
        ReviewEntity approved = review(ReviewStatus.APPROVED, CUSTOMER);
        when(reviewRepository.findByProductIdAndStatus(PRODUCT_ID, ReviewStatus.APPROVED,
                PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(approved)));

        var page = reviewService.listApproved(PRODUCT_ID, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).status()).isEqualTo(ReviewStatus.APPROVED);
    }

    @Test
    void shouldRequireAdminRoleForModeration() {
        setGatewayHeaders(CUSTOMER, "cliente");

        assertThatThrownBy(() -> reviewService.listForModeration(null, PRODUCT_ID, PageRequest.of(0, 10)))
                .isInstanceOf(AdminRoleRequiredException.class);
        assertThatThrownBy(() -> reviewService.moderate(REVIEW_ID,
                new ModerateReviewRequest("approved", null)))
                .isInstanceOf(AdminRoleRequiredException.class);
    }

    @Test
    void shouldApproveReviewRecalculateRatingAndPublishApprovedEvent() {
        setGatewayHeaders(CUSTOMER, "admin");
        ReviewEntity review = review(ReviewStatus.PENDING, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.findAllByProductIdAndStatus(PRODUCT_ID, ReviewStatus.APPROVED))
                .thenReturn(List.of(review));
        when(productRatingRepository.save(any(ProductRatingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = reviewService.moderate(REVIEW_ID, new ModerateReviewRequest("approved", null));

        assertThat(result.status()).isEqualTo(ReviewStatus.APPROVED);
        verify(productRatingRepository).save(any(ProductRatingEntity.class));
        verify(outboxWriter).append(eq(REVIEW_EVENTS), eq(REVIEW_ID.toString()),
                eq("REVIEW_APPROVED"), any(ReviewApprovedEvent.class));
    }

    @Test
    void shouldPublishStatusChangedEventWhenApprovedReviewIsTakenDown() {
        setGatewayHeaders(CUSTOMER, "admin");
        ReviewEntity review = review(ReviewStatus.APPROVED, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.findAllByProductIdAndStatus(PRODUCT_ID, ReviewStatus.APPROVED))
                .thenReturn(List.of());
        when(productRatingRepository.save(any(ProductRatingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = reviewService.moderate(REVIEW_ID, new ModerateReviewRequest("rejected", "spam"));

        assertThat(result.status()).isEqualTo(ReviewStatus.REJECTED);
        assertThat(result.moderationReason()).isEqualTo("spam");
        verify(outboxWriter).append(eq(REVIEW_EVENTS), eq(REVIEW_ID.toString()),
                eq("REVIEW_STATUS_CHANGED"), any(ReviewStatusChangedEvent.class));
    }

    @Test
    void shouldRejectInvalidModerationTransition() {
        setGatewayHeaders(CUSTOMER, "admin");
        ReviewEntity review = review(ReviewStatus.REJECTED, CUSTOMER);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.moderate(REVIEW_ID,
                new ModerateReviewRequest("approved", null)))
                .isInstanceOf(InvalidReviewStatusException.class);
        verifyNoInteractions(outboxWriter, productRatingRepository);
    }

    @Test
    void shouldRemoveAllReviewDataForDeletedProduct() {
        when(reviewRepository.findAllByProductId(PRODUCT_ID))
                .thenReturn(List.of(review(ReviewStatus.APPROVED, CUSTOMER)));

        reviewService.removeProduct(PRODUCT_ID);

        verify(reviewVoteRepository).deleteAllByReviewIdIn(List.of(REVIEW_ID));
        verify(reviewRepository).deleteAllByProductId(PRODUCT_ID);
        verify(productRatingRepository).deleteById(PRODUCT_ID);
    }

    private OrderDto paidOrder() {
        return order("PAID", PRODUCT_ID);
    }

    private OrderDto order(String status, long productId) {
        return new OrderDto(ORDER_ID, status, List.of(new OrderDto.Item(productId)));
    }

    private ReviewEntity review(String status, String customerId) {
        return ReviewEntity.builder()
                .id(REVIEW_ID)
                .productId(PRODUCT_ID)
                .customerId(customerId)
                .orderId(ORDER_ID)
                .rating(5)
                .title("Excelente")
                .body("Muy bueno")
                .status(status)
                .helpfulVotes(0)
                .createdAt(Instant.parse("2026-11-19T10:00:00Z"))
                .updatedAt(Instant.parse("2026-11-19T10:00:00Z"))
                .build();
    }

    private void setGatewayHeaders(String userId, String roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CurrentUser.ID_HEADER, userId);
        request.addHeader(CurrentUser.ROLES_HEADER, roles);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
