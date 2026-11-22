package com.latiendita.paymentService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.paymentService.outbox.OutboxEventRepository;
import com.latiendita.paymentService.payment.PaymentEntity;
import com.latiendita.paymentService.payment.PaymentRepository;
import com.latiendita.paymentService.payment.PaymentStatus;
import com.latiendita.paymentService.provider.ProviderType;
import com.latiendita.paymentService.webhook.PaymentEventRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "outbox.relay.poll-interval-ms=600000",
        "mercado-pago.access-token=TEST-access-token",
        "mercado-pago.webhook-secret=test-webhook-secret"
})
class AdminPaymentApiIntegrationTest {

    private static final String ADMIN_ID = "admin-1";
    private static final String ADMIN_ROLES = "user,admin";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private PaymentEventRepository paymentEventRepository;

    @BeforeEach
    void setup() {
        outboxEventRepository.deleteAll();
        paymentEventRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void adminListReturnsFilteredPaginatedPaymentsOfAllCustomers() throws Exception {
        Instant now = Instant.now();
        paymentRepository.saveAll(List.of(
                payment("cust-alpha", "ORD-100", PaymentStatus.APPROVED,
                        "100.00", now.minus(Duration.ofDays(1))),
                payment("cust-beta", "ORD-200", PaymentStatus.PENDING,
                        "200.00", now.minus(Duration.ofDays(2))),
                payment("cust-alpha", "ORD-300", PaymentStatus.REJECTED,
                        "300.00", now.minus(Duration.ofDays(3)))));

        mockMvc.perform(get("/api/payments/admin")
                        .header("X-User-Id", ADMIN_ID)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .param("q", "ALPHA")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-100"))
                .andExpect(jsonPath("$.content[0].customerId").value("cust-alpha"))
                .andExpect(jsonPath("$.content[0].provider").value("MERCADO_PAGO"))
                .andExpect(jsonPath("$.content[0].paymentMethodId").value("visa"))
                .andExpect(jsonPath("$.content[0].paymentType").value("credit_card"))
                .andExpect(jsonPath("$.content[0].paidAt")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")))
                .andExpect(jsonPath("$.content[0].amount").value(100.00))
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    void adminListFiltersByCustomerIdStatusAndDays() throws Exception {
        Instant now = Instant.now();
        paymentRepository.saveAll(List.of(
                payment("cust-alpha", "ORD-101", PaymentStatus.APPROVED,
                        "100.00", now.minus(Duration.ofDays(1))),
                payment("cust-alpha", "ORD-102", PaymentStatus.REJECTED,
                        "200.00", now.minus(Duration.ofDays(2))),
                payment("cust-alpha", "ORD-103", PaymentStatus.APPROVED,
                        "300.00", now.minus(Duration.ofDays(60))),
                payment("cust-beta", "ORD-104", PaymentStatus.APPROVED,
                        "400.00", now.minus(Duration.ofDays(1)))));

        mockMvc.perform(get("/api/payments/admin")
                        .header("X-User-Id", ADMIN_ID)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .param("q", "cust-alpha")
                        .param("status", "APPROVED")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-101"));
    }

    @Test
    void adminListAcceptsOrderNumberAsSortProperty() throws Exception {
        Instant now = Instant.now();
        paymentRepository.saveAll(List.of(
                payment("cust-alpha", "ORD-201", PaymentStatus.APPROVED,
                        "100.00", now.minus(Duration.ofDays(1))),
                payment("cust-beta", "ORD-202", PaymentStatus.APPROVED,
                        "200.00", now.minus(Duration.ofDays(2)))));

        mockMvc.perform(get("/api/payments/admin")
                        .header("X-User-Id", ADMIN_ID)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .param("sort", "orderNumber,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-202"))
                .andExpect(jsonPath("$.content[1].orderNumber").value("ORD-201"));
    }

    @Test
    void adminListRejectsCallerWithoutAdminRole() throws Exception {
        paymentRepository.save(payment("cust-alpha", "ORD-301", PaymentStatus.APPROVED,
                "100.00", Instant.now()));

        mockMvc.perform(get("/api/payments/admin")
                        .header("X-User-Id", "cust-alpha"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"))
                .andExpect(jsonPath("$.detail").value("Se requiere el rol 'admin' para esta operación"))
                .andExpect(jsonPath("$.content").doesNotExist());
    }

    @Test
    void adminSummaryExposesExtendedKpisWithinDefaultRange() throws Exception {
        Instant now = Instant.now();
        paymentRepository.saveAll(List.of(
                payment("cust-1", "ORD-401", PaymentStatus.APPROVED,
                        "100.00", now.minus(Duration.ofDays(1))),
                payment("cust-2", "ORD-402", PaymentStatus.APPROVED,
                        "200.00", now.minus(Duration.ofDays(2))),
                payment("cust-3", "ORD-403", PaymentStatus.PENDING,
                        "50.00", now.minus(Duration.ofDays(3))),
                payment("cust-4", "ORD-404", PaymentStatus.REJECTED,
                        "25.00", now.minus(Duration.ofDays(4))),
                payment("cust-5", "ORD-405", PaymentStatus.APPROVED,
                        "500.00", now.minus(Duration.ofDays(10))),
                payment("cust-6", "ORD-406", PaymentStatus.APPROVED,
                        "1000.00", now.minus(Duration.ofDays(60)))));

        mockMvc.perform(get("/api/payments/admin/summary")
                        .header("X-User-Id", ADMIN_ID)
                        .header("X-User-Roles", ADMIN_ROLES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentsToClear").value(1800.00))
                .andExpect(jsonPath("$.paymentsInReview").value(1))
                .andExpect(jsonPath("$.totalPayments").value(5))
                .andExpect(jsonPath("$.approvedCount").value(3))
                .andExpect(jsonPath("$.pendingCount").value(1))
                .andExpect(jsonPath("$.rejectedCount").value(1))
                .andExpect(jsonPath("$.approvedVolume").value(800.00))
                .andExpect(jsonPath("$.pendingVolume").value(50.00))
                .andExpect(jsonPath("$.rejectedVolume").value(25.00))
                .andExpect(jsonPath("$.approvalAttempts").value(4))
                .andExpect(jsonPath("$.approvalRate").value(75))
                .andExpect(jsonPath("$.rangeDays").value(30));
    }

    @Test
    void adminSummaryHonorsDaysParameter() throws Exception {
        Instant now = Instant.now();
        paymentRepository.saveAll(List.of(
                payment("cust-1", "ORD-501", PaymentStatus.APPROVED,
                        "100.00", now.minus(Duration.ofDays(1))),
                payment("cust-2", "ORD-502", PaymentStatus.APPROVED,
                        "200.00", now.minus(Duration.ofDays(2))),
                payment("cust-3", "ORD-503", PaymentStatus.PENDING,
                        "50.00", now.minus(Duration.ofDays(3))),
                payment("cust-4", "ORD-504", PaymentStatus.REJECTED,
                        "25.00", now.minus(Duration.ofDays(4))),
                payment("cust-5", "ORD-505", PaymentStatus.APPROVED,
                        "500.00", now.minus(Duration.ofDays(10)))));

        mockMvc.perform(get("/api/payments/admin/summary")
                        .header("X-User-Id", ADMIN_ID)
                        .header("X-User-Roles", ADMIN_ROLES)
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayments").value(4))
                .andExpect(jsonPath("$.approvedCount").value(2))
                .andExpect(jsonPath("$.approvedVolume").value(300.00))
                .andExpect(jsonPath("$.approvalAttempts").value(3))
                .andExpect(jsonPath("$.approvalRate").value(67))
                .andExpect(jsonPath("$.rangeDays").value(7));
    }

    @Test
    void adminSummaryRejectsCallerWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/payments/admin/summary")
                        .header("X-User-Id", "cust-alpha"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"))
                .andExpect(jsonPath("$.totalPayments").doesNotExist());
    }

    @Test
    void customerListRemainsScopedToCurrentUserForAdminCallers() throws Exception {
        paymentRepository.saveAll(List.of(
                payment("cust-alpha", "ORD-601", PaymentStatus.APPROVED,
                        "100.00", Instant.now()),
                payment("cust-beta", "ORD-602", PaymentStatus.APPROVED,
                        "200.00", Instant.now())));

        mockMvc.perform(get("/api/payments")
                        .header("X-User-Id", ADMIN_ID)
                        .header("X-User-Roles", ADMIN_ROLES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private PaymentEntity payment(String customerId, String orderNumber, PaymentStatus status,
                                  String amount, Instant createdAt) {
        Instant paidAt = status == PaymentStatus.APPROVED ? createdAt.plusSeconds(1) : null;
        Instant updatedAt = paidAt != null ? paidAt : createdAt;
        return PaymentEntity.builder()
                .orderId(Long.parseLong(orderNumber.replace("ORD-", "")))
                .customerId(customerId)
                .provider(ProviderType.MERCADO_PAGO)
                .status(status)
                .amount(new BigDecimal(amount))
                .currency("ARS")
                .externalReference(orderNumber)
                .idempotencyKey(orderNumber)
                .paymentMethodId("visa")
                .paymentType("credit_card")
                .paidAt(paidAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
