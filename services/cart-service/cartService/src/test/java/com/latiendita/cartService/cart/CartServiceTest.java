package com.latiendita.cartService.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.cartService.cart.dto.AddCartItemRequest;
import com.latiendita.cartService.cart.dto.CartDto;
import com.latiendita.cartService.cart.dto.UpdateCartItemRequest;
import com.latiendita.cartService.exception.CartItemNotFoundException;
import com.latiendita.cartService.exception.InsufficientStockException;
import com.latiendita.cartService.exception.InvalidQuantityException;
import com.latiendita.cartService.exception.MissingGatewayHeaderException;
import com.latiendita.cartService.exception.ProductNotFoundException;
import com.latiendita.cartService.exception.ProductServiceUnavailableException;
import com.latiendita.cartService.exception.ProductWithoutPriceException;
import com.latiendita.cartService.product.ProductClient;
import com.latiendita.cartService.product.ProductSnapshot;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String USER = "user-1";
    private static final Instant NOW = Instant.parse("2026-11-19T10:00:00Z");

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setCurrentUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", USER);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void resetCurrentUser() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldReturnComputedTotalsForExistingCart() {
        CartEntity cart = cart();
        item(cart, 42L, "10.00", 2, false);
        item(cart, 7L, "5.50", 1, true);
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));

        CartDto dto = cartService.getCart();

        assertThat(dto.id()).isEqualTo(cart.getId());
        assertThat(dto.items()).hasSize(2);
        assertThat(dto.subtotal()).isEqualByComparingTo("25.50");
        assertThat(dto.itemCount()).isEqualTo(3);
        assertThat(dto.total()).isEqualByComparingTo("25.50");
        assertThat(dto.items().get(0).lineSubtotal()).isEqualByComparingTo("20.00");
        assertThat(dto.items().get(1).priceChanged()).isTrue();
        verify(productClient, never()).findById(any());
    }

    @Test
    void shouldReturnEmptyCartWhenUserHasNoCart() {
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.empty());

        CartDto dto = cartService.getCart();

        assertThat(dto.id()).isNull();
        assertThat(dto.items()).isEmpty();
        assertThat(dto.subtotal()).isEqualByComparingTo("0");
        assertThat(dto.itemCount()).isZero();
        assertThat(dto.total()).isEqualByComparingTo("0");
        verify(productClient, never()).findById(any());
    }

    @Test
    void shouldAddNewItemUsingServerPriceAndTitle() {
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.empty());
        when(productClient.findById(42L)).thenReturn(snapshot(42L, "999.99", 5));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDto dto = cartService.addItem(new AddCartItemRequest(42L, 2));

        ArgumentCaptor<CartEntity> saved = ArgumentCaptor.forClass(CartEntity.class);
        verify(cartRepository).save(saved.capture());
        CartEntity cart = saved.getValue();
        assertThat(cart.getUserId()).isEqualTo(USER);
        assertThat(cart.getItems()).hasSize(1);
        CartItemEntity item = cart.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(42L);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice()).isEqualByComparingTo("999.99");
        assertThat(item.getProductTitle()).isEqualTo("Title 42");
        assertThat(item.getImageUrl()).isEqualTo("https://img/42");
        assertThat(item.isPriceChanged()).isFalse();
        assertThat(dto.itemCount()).isEqualTo(2);
        assertThat(dto.subtotal()).isEqualByComparingTo("1999.98");
        assertThat(dto.total()).isEqualByComparingTo("1999.98");
    }

    @Test
    void shouldSumQuantityAndRefreshSnapshotClearingPriceChanged() {
        CartEntity cart = cart();
        CartItemEntity existing = item(cart, 42L, "10.00", 1, true);
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));
        when(productClient.findById(42L)).thenReturn(snapshot(42L, "12.00", 10));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDto dto = cartService.addItem(new AddCartItemRequest(42L, 2));

        assertThat(existing.getQuantity()).isEqualTo(3);
        assertThat(existing.getUnitPrice()).isEqualByComparingTo("12.00");
        assertThat(existing.getProductTitle()).isEqualTo("Title 42");
        assertThat(existing.isPriceChanged()).isFalse();
        assertThat(dto.items().get(0).lineSubtotal()).isEqualByComparingTo("36.00");
        assertThat(dto.itemCount()).isEqualTo(3);
    }

    @Test
    void shouldRejectAggregatedQuantityAbove99() {
        CartEntity cart = cart();
        item(cart, 42L, "10.00", 98, false);
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));
        when(productClient.findById(42L)).thenReturn(snapshot(42L, "10.00", 500));

        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(42L, 2)))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("Quantity must be between 1 and 99");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldRejectQuantityAboveStock() {
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.empty());
        when(productClient.findById(42L)).thenReturn(snapshot(42L, "10.00", 3));

        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(42L, 5)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Requested quantity 5 exceeds available stock 3");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldRejectProductWithoutPrice() {
        when(productClient.findById(42L)).thenReturn(new ProductSnapshot(42L, "Title", null, null, 5));

        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(42L, 1)))
                .isInstanceOf(ProductWithoutPriceException.class)
                .hasMessage("Product 42 has no price assigned");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldPropagateClientFailureOnWrite() {
        when(productClient.findById(42L))
                .thenThrow(new ProductServiceUnavailableException(new RuntimeException("down")));

        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(42L, 1)))
                .isInstanceOf(ProductServiceUnavailableException.class)
                .hasMessage("Product service is not available");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldPropagateMissingProductOnWrite() {
        when(productClient.findById(42L)).thenThrow(new ProductNotFoundException(42L));

        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(42L, 1)))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 42 not found");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldUpdateQuantityAndRefreshSnapshot() {
        CartEntity cart = cart();
        CartItemEntity existing = item(cart, 42L, "10.00", 4, true);
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));
        when(productClient.findById(42L)).thenReturn(snapshot(42L, "11.00", 10));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDto dto = cartService.updateItem(42L, new UpdateCartItemRequest(2));

        assertThat(existing.getQuantity()).isEqualTo(2);
        assertThat(existing.getUnitPrice()).isEqualByComparingTo("11.00");
        assertThat(existing.isPriceChanged()).isFalse();
        assertThat(dto.subtotal()).isEqualByComparingTo("22.00");
    }

    @Test
    void shouldRejectUpdateForMissingItem() {
        CartEntity cart = cart();
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItem(42L, new UpdateCartItemRequest(2)))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessage("Cart item for product 42 not found");

        verify(productClient, never()).findById(any());
    }

    @Test
    void shouldRemoveItem() {
        CartEntity cart = cart();
        CartItemEntity kept = item(cart, 42L, "10.00", 1, false);
        item(cart, 7L, "5.00", 1, false);
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.removeItem(7L);

        assertThat(cart.getItems()).containsExactly(kept);
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldClearCart() {
        CartEntity cart = cart();
        item(cart, 42L, "10.00", 1, false);
        item(cart, 7L, "5.00", 1, false);
        when(cartRepository.findByUserId(USER)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.clearCart();

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldRejectRequestWithoutGatewayIdentity() {
        RequestContextHolder.resetRequestAttributes();

        assertThatThrownBy(() -> cartService.getCart())
                .isInstanceOf(MissingGatewayHeaderException.class)
                .hasMessage("Missing gateway identity header");
    }

    @Test
    void shouldValidateQuantityRangeAtRequestBoundary() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<AddCartItemRequest>> tooHigh =
                validator.validate(new AddCartItemRequest(42L, 100));
        Set<ConstraintViolation<AddCartItemRequest>> tooLow =
                validator.validate(new AddCartItemRequest(42L, 0));
        Set<ConstraintViolation<UpdateCartItemRequest>> update =
                validator.validate(new UpdateCartItemRequest(100));

        assertThat(tooHigh).extracting(ConstraintViolation::getMessage)
                .contains("Quantity must be between 1 and 99");
        assertThat(tooLow).extracting(ConstraintViolation::getMessage)
                .contains("Quantity must be between 1 and 99");
        assertThat(update).extracting(ConstraintViolation::getMessage)
                .contains("Quantity must be between 1 and 99");
    }

    private CartEntity cart() {
        return CartEntity.builder()
                .id(UUID.randomUUID())
                .userId(USER)
                .createdAt(NOW)
                .updatedAt(NOW)
                .version(0L)
                .build();
    }

    private CartItemEntity item(CartEntity cart, long productId, String price, int quantity,
                                boolean priceChanged) {
        CartItemEntity item = CartItemEntity.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .productId(productId)
                .productTitle("Old title " + productId)
                .imageUrl("https://old/" + productId)
                .quantity(quantity)
                .unitPrice(new BigDecimal(price))
                .priceChanged(priceChanged)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
        cart.addItem(item);
        return item;
    }

    private ProductSnapshot snapshot(long productId, String price, int stock) {
        return new ProductSnapshot(productId, "Title " + productId,
                "https://img/" + productId, new BigDecimal(price), stock);
    }
}
