package com.latiendita.cartService.cart;

import com.latiendita.cartService.cart.event.ProductDeletedEvent;
import com.latiendita.cartService.cart.event.ProductPriceChangedEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    public static final String PRODUCT_PRICE_CHANGED = "PRODUCT_PRICE_CHANGED";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";

    private final CartItemRepository cartItemRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PRODUCT_EVENTS_TOPIC)
    @Transactional
    public void onProductEvent(String payload) {
        String type = readEventType(payload);
        if (PRODUCT_PRICE_CHANGED.equals(type)) {
            applyPriceChange(payload);
        } else if (PRODUCT_DELETED.equals(type)) {
            removeProduct(payload);
        }
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

    private void applyPriceChange(String payload) {
        ProductPriceChangedEvent event;
        try {
            event = objectMapper.readValue(payload, ProductPriceChangedEvent.class);
        } catch (Exception ex) {
            log.warn("Malformed PRODUCT_PRICE_CHANGED event discarded: {}", payload);
            return;
        }
        if (event.productId() == null || event.price() == null) {
            log.warn("Incomplete PRODUCT_PRICE_CHANGED event discarded: {}", payload);
            return;
        }

        List<CartItemEntity> items = cartItemRepository.findAllByProductId(event.productId());
        for (CartItemEntity item : items) {
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(event.price()) != 0) {
                item.setUnitPrice(event.price());
                item.setPriceChanged(true);
                item.setUpdatedAt(Instant.now());
                cartItemRepository.save(item);
            }
        }
    }

    private void removeProduct(String payload) {
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

        List<CartItemEntity> items = cartItemRepository.findAllByProductId(event.productId());
        if (!items.isEmpty()) {
            cartItemRepository.deleteAll(items);
        }
    }
}
