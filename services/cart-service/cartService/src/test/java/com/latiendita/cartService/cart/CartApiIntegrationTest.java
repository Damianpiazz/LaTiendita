package com.latiendita.cartService.cart;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.cartService.TestcontainersConfiguration;
import com.latiendita.cartService.cart.dto.AddCartItemRequest;
import com.latiendita.cartService.cart.dto.UpdateCartItemRequest;
import com.latiendita.cartService.exception.ProductNotFoundException;
import com.latiendita.cartService.exception.ProductServiceUnavailableException;
import com.latiendita.cartService.product.ProductClient;
import com.latiendita.cartService.product.ProductSnapshot;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class CartApiIntegrationTest {

    private static final String USER = "user-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @MockitoBean
    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        lenient().when(productClient.findById(42L)).thenReturn(
                new ProductSnapshot(42L, "Notebook", "https://img/42.png",
                        new BigDecimal("999.99"), 5));
    }

    @Test
    void shouldAddItemReturning200WithComputedTotals() throws Exception {
        mockMvc.perform(post("/api/carts/items")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new AddCartItemRequest(42L, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.items[0].productId").value(42))
                .andExpect(jsonPath("$.items[0].productTitle").value("Notebook"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(999.99))
                .andExpect(jsonPath("$.items[0].lineSubtotal").value(1999.98))
                .andExpect(jsonPath("$.items[0].priceChanged").value(false))
                .andExpect(jsonPath("$.subtotal").value(1999.98))
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.total").value(1999.98));
    }

    @Test
    void shouldGetCurrentCart() throws Exception {
        addItem(2);

        mockMvc.perform(get("/api/carts").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.subtotal").value(1999.98));
    }

    @Test
    void shouldUpdateItemReturning200() throws Exception {
        addItem(4);

        mockMvc.perform(patch("/api/carts/items/{productId}", 42L)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new UpdateCartItemRequest(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.subtotal").value(1999.98));
    }

    @Test
    void shouldRemoveItemAndClearCartReturning204() throws Exception {
        addItem(1);

        mockMvc.perform(delete("/api/carts/items/{productId}", 42L).header("X-User-Id", USER))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/carts").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        addItem(1);

        mockMvc.perform(delete("/api/carts").header("X-User-Id", USER))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/carts").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void shouldRejectMissingIdentityHeaderWith403() throws Exception {
        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value("Missing gateway identity header"));
    }

    @Test
    void shouldRejectInvalidQuantityWith400() throws Exception {
        mockMvc.perform(post("/api/carts/items")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new AddCartItemRequest(42L, 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(
                        containsString("Quantity must be between 1 and 99")));
    }

    @Test
    void shouldReturn404WhenCartItemDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/carts/items/{productId}", 99L)
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new UpdateCartItemRequest(1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Cart item for product 99 not found"));
    }

    @Test
    void shouldReturn409WhenQuantityExceedsStock() throws Exception {
        mockMvc.perform(post("/api/carts/items")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new AddCartItemRequest(42L, 6))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Requested quantity 6 exceeds available stock 5"));
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception {
        when(productClient.findById(42L)).thenThrow(new ProductNotFoundException(42L));

        mockMvc.perform(post("/api/carts/items")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new AddCartItemRequest(42L, 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Product with id 42 not found"));
    }

    @Test
    void shouldReturn503WhenProductServiceIsUnavailable() throws Exception {
        when(productClient.findById(42L))
                .thenThrow(new ProductServiceUnavailableException(new RuntimeException("down")));

        mockMvc.perform(post("/api/carts/items")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new AddCartItemRequest(42L, 1))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Service Unavailable"))
                .andExpect(jsonPath("$.detail").value("Product service is not available"));
    }

    private void addItem(int quantity) throws Exception {
        mockMvc.perform(post("/api/carts/items")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new AddCartItemRequest(42L, quantity))))
                .andExpect(status().isOk());
    }

    private String body(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
