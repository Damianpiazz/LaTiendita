package com.latiendita.reviewService.review;

import com.latiendita.reviewService.review.event.ProductDeletedEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventsConsumer {

    public static final String PRODUCT_EVENTS_TOPIC = "product-events";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ProductRatingRepository productRatingRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PRODUCT_EVENTS_TOPIC)
    @Transactional
    public void onProductEvent(String payload) {
        String type = readEventType(payload);
        if (!PRODUCT_DELETED.equals(type)) {
            return;
        }
        ProductDeletedEvent event;
        try {
            event = objectMapper.readValue(payload, ProductDeletedEvent.class);
        } catch (Exception ex) {
            log.warn("Malformed PRODUCT_DELETED event discarded: {}", payload);
            return;
        }
        if (event.productId() == null) {
            log.warn("Incomplete PRODUCT_DELETED event discarded: {}", payload);
            return;
        }

        List<UUID> reviewIds = reviewRepository.findAllByProductId(event.productId()).stream()
                .map(ReviewEntity::getId)
                .toList();
        if (!reviewIds.isEmpty()) {
            reviewVoteRepository.deleteAllByReviewIdIn(reviewIds);
        }
        reviewRepository.deleteAllByProductId(event.productId());
        productRatingRepository.deleteById(event.productId());
    }

    private String readEventType(String payload) {
        try {
            Map<?, ?> node = objectMapper.readValue(payload, Map.class);
            Object type = node.get("type");
            return type == null ? null : type.toString();
        } catch (Exception ex) {
            log.warn("Malformed message in product-events discarded: {}", payload);
            return null;
        }
    }
}
