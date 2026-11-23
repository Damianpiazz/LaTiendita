package com.latiendita.reviewService.review;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.reviewService.TestcontainersConfiguration;
import com.latiendita.reviewService.order.OrderClient;
import com.latiendita.reviewService.order.OrderDto;
import com.latiendita.reviewService.product.ProductClient;
import com.latiendita.reviewService.review.dto.CreateReviewRequest;
import com.latiendita.reviewService.review.dto.ModerateReviewRequest;
import java.util.List;
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
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "outbox.relay.poll-interval-ms=60000"
})
@AutoConfigureMockMvc
class ReviewApiIntegrationTest {

    private static final String CUSTOMER = "auth0|customer-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewVoteRepository reviewVoteRepository;

    @Autowired
    private ProductRatingRepository productRatingRepository;

    @MockitoBean
    private OrderClient orderClient;

    @MockitoBean
    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        reviewVoteRepository.deleteAll();
        reviewRepository.deleteAll();
        productRatingRepository.deleteAll();
        lenient().when(orderClient.findPurchases()).thenReturn(List.of(
                new OrderDto(9L, "PAID", List.of(new OrderDto.Item(42L)))));
    }

    @Test
    void shouldReturnZeroedSummaryForProductWithoutReviews() throws Exception {
        mockMvc.perform(get("/api/reviews/summary").param("productId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(42))
                .andExpect(jsonPath("$.average").value(0.00))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.distribution.1").value(0));
    }

    @Test
    void shouldExposeOnlyTheAuthenticatedCustomersReviews() throws Exception {
        String id = createReview();

        mockMvc.perform(get("/api/reviews/me").header("X-User-Id", CUSTOMER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].status").value("pending"));

        mockMvc.perform(get("/api/reviews/me").header("X-User-Id", "another-customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldCreateReviewAndExposeItOnlyAfterApproval() throws Exception {
        String id = createReview();

        mockMvc.perform(get("/api/reviews").param("productId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/admin/reviews")
                        .param("status", "pending")
                        .header("X-User-Id", "admin-1")
                        .header("X-User-Roles", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].status").value("pending"));

        mockMvc.perform(patch("/api/admin/reviews/{id}/status", id)
                        .header("X-User-Id", "admin-1")
                        .header("X-User-Roles", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new ModerateReviewRequest("approved", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));

        mockMvc.perform(get("/api/reviews").param("productId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("approved"));

        mockMvc.perform(get("/api/reviews/summary").param("productId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(5.00))
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.distribution.5").value(1));
    }

    @Test
    void shouldRejectReviewWithoutGatewayHeader() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new CreateReviewRequest(42L, 5, "Excelente", "Muy bueno"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDuplicateHelpfulVoteWithConflict() throws Exception {
        String id = createReview();
        approve(id);

        mockMvc.perform(post("/api/reviews/{id}/helpful", id).header("X-User-Id", "voter-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpfulVotes").value(1));

        mockMvc.perform(post("/api/reviews/{id}/helpful", id).header("X-User-Id", "voter-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("already voted")));
    }

    private String createReview() throws Exception {
        var response = mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", CUSTOMER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new CreateReviewRequest(42L, 5, "Excelente", "Muy bueno"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn();
        return objectMapper.readTree(response.getResponse().getContentAsString()).get("id").asText();
    }

    private void approve(String id) throws Exception {
        mockMvc.perform(patch("/api/admin/reviews/{id}/status", id)
                        .header("X-User-Id", "admin-1")
                        .header("X-User-Roles", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new ModerateReviewRequest("approved", null))))
                .andExpect(status().isOk());
    }

    private String body(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
