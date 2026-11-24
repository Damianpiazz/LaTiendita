package com.latiendita.cartService.cart;

import com.latiendita.cartService.CurrentUser;
import com.latiendita.cartService.cart.dto.AddCartItemRequest;
import com.latiendita.cartService.cart.dto.CartDto;
import com.latiendita.cartService.cart.dto.CartMapper;
import com.latiendita.cartService.cart.dto.UpdateCartItemRequest;
import com.latiendita.cartService.exception.CartItemNotFoundException;
import com.latiendita.cartService.exception.InsufficientStockException;
import com.latiendita.cartService.exception.InvalidQuantityException;
import com.latiendita.cartService.exception.ProductWithoutPriceException;
import com.latiendita.cartService.product.ProductClient;
import com.latiendita.cartService.product.ProductSnapshot;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private static final int MAX_QUANTITY = 99;

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Transactional(readOnly = true)
    public CartDto getCart() {
        String userId = CurrentUser.id();
        return cartRepository.findByUserId(userId)
                .map(CartMapper::toDto)
                .orElseGet(CartMapper::emptyDto);
    }

    public CartDto addItem(AddCartItemRequest request) {
        String userId = CurrentUser.id();
        ProductSnapshot product = productClient.findById(request.productId());
        validatePriced(product);

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> newCart(userId));
        CartItemEntity item = findItem(cart, request.productId()).orElse(null);
        int currentQuantity = item == null ? 0 : item.getQuantity();
        int newQuantity = currentQuantity + request.quantity();
        validateQuantity(request.productId(), newQuantity, product.stock());

        Instant now = Instant.now();
        if (item == null) {
            cart.addItem(CartItemEntity.builder()
                    .id(UUID.randomUUID())
                    .productId(product.productId())
                    .productTitle(product.title())
                    .imageUrl(product.imageUrl())
                    .quantity(request.quantity())
                    .unitPrice(product.price())
                    .priceChanged(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        } else {
            item.setQuantity(newQuantity);
            refreshSnapshot(item, product, now);
        }
        cart.setUpdatedAt(now);
        return CartMapper.toDto(cartRepository.save(cart));
    }

    public CartDto updateItem(Long productId, UpdateCartItemRequest request) {
        String userId = CurrentUser.id();
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
        CartItemEntity item = findItem(cart, productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        ProductSnapshot product = productClient.findById(productId);
        validatePriced(product);
        validateQuantity(productId, request.quantity(), product.stock());

        Instant now = Instant.now();
        item.setQuantity(request.quantity());
        refreshSnapshot(item, product, now);
        cart.setUpdatedAt(now);
        return CartMapper.toDto(cartRepository.save(cart));
    }

    public void removeItem(Long productId) {
        String userId = CurrentUser.id();
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
        CartItemEntity item = findItem(cart, productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        cart.removeItem(item);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    public void clearCart() {
        String userId = CurrentUser.id();
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
        });
    }

    private CartEntity newCart(String userId) {
        Instant now = Instant.now();
        return CartEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
    }

    private Optional<CartItemEntity> findItem(CartEntity cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private void validatePriced(ProductSnapshot product) {
        if (product.price() == null) {
            throw new ProductWithoutPriceException(product.productId());
        }
    }

    private void validateQuantity(Long productId, int quantity, int stock) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new InvalidQuantityException();
        }
        if (quantity > stock) {
            throw new InsufficientStockException(quantity, stock);
        }
    }

    private void refreshSnapshot(CartItemEntity item, ProductSnapshot product, Instant now) {
        item.setProductTitle(product.title());
        item.setImageUrl(product.imageUrl());
        item.setUnitPrice(product.price());
        item.setPriceChanged(false);
        item.setUpdatedAt(now);
    }
}
