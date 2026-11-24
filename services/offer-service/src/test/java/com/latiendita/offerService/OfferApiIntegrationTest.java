package com.latiendita.offerService;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.offerService.benefit.BenefitEntity;
import com.latiendita.offerService.benefit.BenefitRepository;
import com.latiendita.offerService.coupon.CouponRepository;
import com.latiendita.offerService.promotion.PromotionRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"eureka.client.enabled=false", "outbox.relay.poll-interval-ms=600000"})
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.kafka.listener.auto-startup=false")
class OfferApiIntegrationTest {

    private static final String PROMOTION = """
            {"code":"SAVE10","name":"Save 10","type":"PERCENTAGE","scope":"ORDER",\
            "discountValue":10.00,"minSubtotal":50.00,\
            "startsAt":"2026-01-01T00:00:00Z","endsAt":"2099-01-01T00:00:00Z",\
            "priority":5,"stackable":true,"active":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @BeforeEach
    void setup() {
        couponRepository.deleteAll();
        promotionRepository.deleteAll();
        benefitRepository.deleteAll();
    }

    @Test
    void shouldCreatePromotionReturning201WithLocation() throws Exception {
        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROMOTION))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/api/admin/promotions/")))
                .andExpect(jsonPath("$.code").value("SAVE10"))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn400ProblemDetailForInvalidPromotion() throws Exception {
        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"SAVE10","name":"","type":"PERCENTAGE","scope":"ORDER",\
                                "startsAt":"2026-01-01T00:00:00Z","priority":1,\
                                "stackable":true,"active":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturn409ProblemDetailForDuplicateCode() throws Exception {
        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTION))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTION))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(containsString("SAVE10")));
    }

    @Test
    void shouldReturn404ProblemDetailForUnknownPromotion() throws Exception {
        mockMvc.perform(put("/api/admin/promotions/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTION))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Promotion not found"));
    }

    @Test
    void shouldReturn404ProblemDetailForUnknownCoupon() throws Exception {
        mockMvc.perform(get("/api/coupons/{code}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Coupon not found"));
    }

    @Test
    void shouldListActivePromotionsAndEvaluateACart() throws Exception {
        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTION))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE10"));

        mockMvc.perform(post("/api/promotions/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":1,"categoryId":10,\
                                "quantity":2,"unitPrice":100.00}],"subtotal":200.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.appliedAmount").value(20.00))
                .andExpect(jsonPath("$.eligible").isArray());
    }

    @Test
    void shouldValidateCouponAndRecordUsageCount() throws Exception {
        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTION))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/coupons/{code}", "SAVE10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE10"))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.usageCount").value(0));
    }

    @Test
    void shouldReturnBenefitsSortedBySortOrder() throws Exception {
        benefitRepository.save(benefit("RETURNS", 2));
        benefitRepository.save(benefit("SECURE_PAYMENTS", 1));

        mockMvc.perform(get("/api/benefits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SECURE_PAYMENTS"))
                .andExpect(jsonPath("$[1].code").value("RETURNS"));
    }

    @Test
    void shouldDeactivatePromotionReturning204() throws Exception {
        String location = mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTION))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(delete("/api/admin/promotions/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private BenefitEntity benefit(String code, int sortOrder) {
        Instant now = Instant.now();
        return BenefitEntity.builder()
                .code(code)
                .title(code)
                .description("Description " + code)
                .icon("icon")
                .sortOrder(sortOrder)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
