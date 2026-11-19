package com.latiendita.reviewService.review;

import com.latiendita.reviewService.CurrentUser;
import com.latiendita.reviewService.exception.DuplicateReviewException;
import com.latiendita.reviewService.exception.DuplicateVoteException;
import com.latiendita.reviewService.exception.InvalidRatingException;
import com.latiendita.reviewService.exception.InvalidReviewStatusException;
import com.latiendita.reviewService.exception.ModerationReasonRequiredException;
import com.latiendita.reviewService.exception.NotPurchasedException;
import com.latiendita.reviewService.exception.OrderNotEligibleException;
import com.latiendita.reviewService.exception.ReviewNotVotableException;
import com.latiendita.reviewService.exception.ReviewNotFoundException;
import com.latiendita.reviewService.exception.SelfVoteException;
import com.latiendita.reviewService.order.OrderClient;
import com.latiendita.reviewService.order.OrderDto;
import com.latiendita.reviewService.outbox.OutboxWriter;
import com.latiendita.reviewService.product.ProductClient;
import com.latiendita.reviewService.review.dto.CreateReviewRequest;
import com.latiendita.reviewService.review.dto.ModerateReviewRequest;
import com.latiendita.reviewService.review.dto.ReviewDto;
import com.latiendita.reviewService.review.dto.ReviewMapper;
import com.latiendita.reviewService.review.dto.ReviewSummaryDto;
import com.latiendita.reviewService.review.event.ReviewApprovedEvent;
import com.latiendita.reviewService.review.event.ReviewStatusChangedEvent;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    public static final String REVIEW_EVENTS_TOPIC = "review-events";
    public static final String REVIEW_APPROVED = "REVIEW_APPROVED";
    public static final String REVIEW_STATUS_CHANGED = "REVIEW_STATUS_CHANGED";

    private static final Set<String> ELIGIBLE_ORDER_STATUSES =
            Set.of("PAID", "SHIPPED", "DELIVERED");

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ProductRatingRepository productRatingRepository;
    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final OutboxWriter outboxWriter;

    public ReviewDto create(CreateReviewRequest request) {
        String customerId = CurrentUser.id();
        if (request.rating() < 1 || request.rating() > 5) {
            throw new InvalidRatingException(request.rating());
        }

        productClient.requireExisting(request.productId());
        List<OrderDto> purchases = orderClient.findPurchases();

        OrderDto eligible = purchases.stream()
                .filter(order -> containsProduct(order, request.productId()))
                .filter(order -> eligibleStatus(order.status()))
                .findFirst()
                .orElse(null);
        if (eligible == null) {
            OrderDto purchased = purchases.stream()
                    .filter(order -> containsProduct(order, request.productId()))
                    .findFirst()
                    .orElse(null);
            if (purchased != null) {
                throw new OrderNotEligibleException(orderId(purchased), purchased.status());
            }
            throw new NotPurchasedException(request.productId());
        }

        if (reviewRepository.existsByCustomerIdAndProductId(customerId, request.productId())) {
            throw new DuplicateReviewException(request.productId());
        }

        Instant now = Instant.now();
        ReviewEntity review = ReviewEntity.builder()
                .id(UUID.randomUUID())
                .productId(request.productId())
                .customerId(customerId)
                .orderId(orderId(eligible))
                .rating(request.rating())
                .title(request.title().trim())
                .body(request.body().trim())
                .status(ReviewStatus.PENDING)
                .helpfulVotes(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return ReviewMapper.toDto(reviewRepository.save(review));
    }

    public ReviewDto markHelpful(UUID reviewId) {
        String voter = CurrentUser.id();
        ReviewEntity review = requireReview(reviewId);
        if (!ReviewStatus.APPROVED.equals(review.getStatus())) {
            throw new ReviewNotVotableException(reviewId);
        }
        if (review.getCustomerId().equals(voter)) {
            throw new SelfVoteException();
        }
        if (reviewVoteRepository.existsByReviewIdAndCustomerId(reviewId, voter)) {
            throw new DuplicateVoteException(reviewId);
        }

        reviewVoteRepository.save(ReviewVoteEntity.builder()
                .id(UUID.randomUUID())
                .reviewId(reviewId)
                .customerId(voter)
                .createdAt(Instant.now())
                .build());
        review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        review.setUpdatedAt(Instant.now());
        return ReviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDto summary(Long productId) {
        return productRatingRepository.findById(productId)
                .map(ReviewMapper::toSummary)
                .orElseGet(() -> ReviewMapper.toSummary(ProductRatingEntity.empty(productId)));
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> listApproved(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)
                .map(ReviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> listForModeration(String status, Long productId, Pageable pageable) {
        CurrentUser.requireAdmin();
        Specification<ReviewEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (productId != null) {
                predicates.add(cb.equal(root.get("productId"), productId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return reviewRepository.findAll(spec, pageable).map(ReviewMapper::toDto);
    }

    public ReviewDto moderate(UUID reviewId, ModerateReviewRequest request) {
        CurrentUser.requireAdmin();
        ReviewEntity review = requireReview(reviewId);
        String from = review.getStatus();
        String target = request.status();

        if (!canTransition(from, target)) {
            throw new InvalidReviewStatusException(from, target);
        }
        if (ReviewStatus.REJECTED.equals(target)
                && (request.reason() == null || request.reason().isBlank())) {
            throw new ModerationReasonRequiredException();
        }

        review.setStatus(target);
        review.setModerationReason(ReviewStatus.REJECTED.equals(target) ? request.reason().trim() : null);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        List<ReviewEntity> approved = reviewRepository.findAllByProductIdAndStatus(
                review.getProductId(), ReviewStatus.APPROVED);
        ProductRatingEntity rating = ProductRatingEntity.from(review.getProductId(), approved);
        productRatingRepository.save(rating);

        if (ReviewStatus.APPROVED.equals(target)) {
            outboxWriter.append(REVIEW_EVENTS_TOPIC, reviewId.toString(), REVIEW_APPROVED,
                    new ReviewApprovedEvent(REVIEW_APPROVED, reviewId, review.getProductId(),
                            review.getRating(), rating.getAverage(), rating.getReviewCount(),
                            Instant.now()));
        } else if (ReviewStatus.APPROVED.equals(from)) {
            outboxWriter.append(REVIEW_EVENTS_TOPIC, reviewId.toString(), REVIEW_STATUS_CHANGED,
                    new ReviewStatusChangedEvent(REVIEW_STATUS_CHANGED, reviewId,
                            review.getProductId(), target, rating.getAverage(),
                            rating.getReviewCount(), Instant.now()));
        }
        return ReviewMapper.toDto(review);
    }

    /** Called when product-service reports PRODUCT_DELETED. */
    public void removeProduct(Long productId) {
        List<UUID> reviewIds = reviewRepository.findAllByProductId(productId).stream()
                .map(ReviewEntity::getId)
                .toList();
        if (!reviewIds.isEmpty()) {
            reviewVoteRepository.deleteAllByReviewIdIn(reviewIds);
        }
        reviewRepository.deleteAllByProductId(productId);
        productRatingRepository.deleteById(productId);
    }

    private ReviewEntity requireReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }

    private boolean canTransition(String from, String target) {
        if (ReviewStatus.PENDING.equals(from)) {
            return ReviewStatus.APPROVED.equals(target) || ReviewStatus.REJECTED.equals(target);
        }
        return ReviewStatus.APPROVED.equals(from) && ReviewStatus.REJECTED.equals(target);
    }

    private boolean containsProduct(OrderDto order, Long productId) {
        return order.items() != null && order.items().stream()
                .anyMatch(item -> productId.equals(item.productId()));
    }

    private boolean eligibleStatus(String status) {
        return status != null && ELIGIBLE_ORDER_STATUSES.contains(status.toUpperCase());
    }

    private Long orderId(OrderDto order) {
        return order.id() == null ? 0L : order.id();
    }
}
