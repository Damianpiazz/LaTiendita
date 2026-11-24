package com.latiendita.customerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.latiendita.customerService.customer.CustomerEntity;
import com.latiendita.customerService.customer.CustomerRepository;
import com.latiendita.customerService.customer.CustomerStatus;
import com.latiendita.customerService.customer.order.CustomerOrderStats;
import com.latiendita.customerService.customer.order.CustomerOrderSummary;
import com.latiendita.customerService.customer.order.OrderClient;
import com.latiendita.customerService.customer.dto.AdminCustomerResponse;
import com.latiendita.customerService.customer.dto.AdminCustomerSummaryResponse;
import com.latiendita.customerService.customer.dto.CustomerSegment;
import com.latiendita.customerService.customer.AdminCustomerService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AdminCustomerServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:30:00Z");

    private static String customerId(String key) {
        return "00000000-0000-0000-0000-00000000000" + key.substring(1);
    }

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderClient orderClient;

    @Spy
    private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @InjectMocks
    private AdminCustomerService adminCustomerService;

    @BeforeEach
    void feedCustomers() {
        when(customerRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(
                customer("c1", "Ana", "Diaz", "ana@mail.com", CustomerStatus.ACTIVE,
                        NOW.minus(java.time.Duration.ofDays(10))),
                customer("c2", "Beto", "Perez", "beto@mail.com", CustomerStatus.ACTIVE,
                        NOW.minus(java.time.Duration.ofDays(10))),
                customer("c3", "Ceci", "Lopez", "ceci@mail.com", CustomerStatus.ACTIVE,
                        NOW.minus(java.time.Duration.ofDays(60))),
                customer("c4", "Dani", "Ruiz", "dani@mail.com", CustomerStatus.SUSPENDED,
                        NOW.minus(java.time.Duration.ofDays(60)))));
        when(orderClient.stats(anyList())).thenAnswer(invocation -> {
            List<String> requestedIds = invocation.getArgument(0);
            return List.of(
                    stats("c1", 2, "150.00"),
                    stats("c2", 1, "50.00"),
                    stats("c3", 1, "20.00")).stream()
                    .filter(stat -> requestedIds.contains(stat.customerId()))
                    .toList();
        });
    }

    @Test
    void shouldDeriveSegmentsFromOrderCountCreationDateAndStatus() {
        var page = adminCustomerService.list(null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(AdminCustomerResponse::segment)
                .containsExactly(CustomerSegment.RECURRENTE, CustomerSegment.NUEVA,
                        CustomerSegment.ACTIVA, CustomerSegment.SIN_COMPRAS);
        AdminCustomerResponse first = page.getContent().get(0);
        assertThat(first.customerId()).isEqualTo(customerId("c1"));
        assertThat(first.phone()).isEqualTo("555-1000");
        assertThat(first.orderCount()).isEqualTo(2L);
        assertThat(first.totalSpent()).isEqualByComparingTo("150.00");
        assertThat(first.lastPurchaseAt()).isEqualTo(NOW);
        assertThat(first.createdAt()).isEqualTo(NOW.minus(java.time.Duration.ofDays(10)));
    }

    @Test
    void shouldPrioritizeSinComprasAndKeepSuspendedWithOrdersAsInactiva() {
        when(orderClient.stats(anyList())).thenReturn(List.of(
                noStats("c1"), noStats("c2"), noStats("c3"), stats("c4", 1, "20.00")));

        var page = adminCustomerService.list(null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c1"), customerId("c2"), customerId("c3"),
                        customerId("c4"));
        assertThat(page.getContent()).extracting(AdminCustomerResponse::segment)
                .containsExactly(CustomerSegment.SIN_COMPRAS, CustomerSegment.SIN_COMPRAS,
                        CustomerSegment.SIN_COMPRAS, CustomerSegment.INACTIVA);
        assertThat(adminCustomerService.list(null, null, null, null, "sin-compras",
                PageRequest.of(0, 20)).getContent())
                .extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c1"), customerId("c2"), customerId("c3"));
        assertThat(adminCustomerService.list(null, null, null, null, "inactiva",
                PageRequest.of(0, 20)).getContent())
                .extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c4"));
    }

    @Test
    void shouldFilterByNameEmailAndStatus() {
        var page = adminCustomerService.list("ana", "ana@mail", null, CustomerStatus.ACTIVE, null,
                PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c1"));
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void shouldFilterByPhoneParameterAcrossListSummaryAndCsv() {
        var page = adminCustomerService.list(null, null, "  555-2000  ", null, null,
                PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c2"));
        assertThat(page.getTotalElements()).isEqualTo(1L);

        AdminCustomerSummaryResponse summary =
                adminCustomerService.summary(null, null, "555-2000", null, null);
        assertThat(summary.totalCustomers()).isEqualTo(1L);
        assertThat(summary.recurring()).isZero();
        assertThat(summary.averageTicket()).isEqualByComparingTo("50.00");

        String csv = adminCustomerService.exportCsv(
                null, null, "555-2000", null, null, "orderCount,desc");
        assertThat(csv.lines().count()).isEqualTo(2L);
        assertThat(csv).contains(customerId("c2"), "555-2000", "Beto")
                .doesNotContain("Ana", "Ceci", "Dani");
    }

    @Test
    void shouldFilterByDerivedSegment() {
        var page = adminCustomerService.list(null, null, null, null, "recurrente",
                PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c1"));
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void shouldSortAndPaginateAfterJoiningOrderStats() {
        when(orderClient.stats(anyList())).thenReturn(List.of(
                stats("c1", 1, "10.00"),
                stats("c2", 3, "30.00"),
                stats("c3", 2, "20.00")));

        var page = adminCustomerService.list(null, null, null, null, null,
                PageRequest.of(1, 2, Sort.by(Sort.Order.desc("orderCount"))));

        assertThat(page.getTotalElements()).isEqualTo(4L);
        assertThat(page.getContent()).extracting(AdminCustomerResponse::orderCount)
                .containsExactly(1L, 0L);
    }

    @Test
    void shouldSummarizeCustomerOwnedKpisAndComposeOrderOwnedKpis() {
        when(orderClient.summary()).thenReturn(new CustomerOrderSummary(3, new BigDecimal("125.00")));

        AdminCustomerSummaryResponse summary = adminCustomerService.summary(null, null, null, null, null);

        assertThat(summary.totalCustomers()).isEqualTo(4L);
        assertThat(summary.newThisMonth()).isEqualTo(2L);
        assertThat(summary.recurring()).isEqualTo(3L);
        assertThat(summary.averageTicket()).isEqualByComparingTo("125.00");
    }

    @Test
    void shouldSummarizeFilteredCustomersUsingFilteredOrderStats() {
        when(orderClient.stats(anyList())).thenAnswer(invocation -> {
            List<String> requestedIds = invocation.getArgument(0);
            return List.of(
                    stats("c1", 2, "150.00"),
                    stats("c2", 1, "50.00")).stream()
                    .filter(stat -> requestedIds.contains(stat.customerId()))
                    .toList();
        });

        AdminCustomerSummaryResponse summary = adminCustomerService.summary(
                null, null, null, CustomerStatus.ACTIVE, "recurrente");

        assertThat(summary.totalCustomers()).isEqualTo(1L);
        assertThat(summary.newThisMonth()).isEqualTo(1L);
        assertThat(summary.recurring()).isEqualTo(1L);
        assertThat(summary.averageTicket()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldSearchByPhoneFromTheNameParameter() {
        var page = adminCustomerService.list("555-1000", null, null, null, null,
                PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(AdminCustomerResponse::customerId)
                .containsExactly(customerId("c1"));
    }

    @Test
    void shouldExportFilteredRowsAsCsv() {
        when(orderClient.stats(anyList())).thenReturn(List.of(
                stats("c1", 2, "150.00"),
                stats("c2", 1, "50.00")));

        String csv = adminCustomerService.exportCsv("ana", null, null, CustomerStatus.ACTIVE, null,
                "orderCount,desc");

        assertThat(csv.lines().findFirst()).hasValueSatisfying(header -> assertThat(header)
                .contains("customerId", "segment", "orderCount", "totalSpent", "lastPurchaseAt"));
        assertThat(csv).contains(customerId("c1"), "Ana", "Diaz", "ana@mail.com", "recurrente");
        assertThat(csv).doesNotContain("Beto", "Ceci", "Dani");
    }

    private CustomerEntity customer(String id, String firstName, String lastName, String email,
                                    CustomerStatus status, Instant createdAt) {
        return CustomerEntity.builder()
                .id(java.util.UUID.fromString(customerId(id)))
                .authorizerSub("sub-" + id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone("555-" + id.substring(1) + "000")
                .status(status)
                .newsletterOptIn(false)
                .lastLoginAt(createdAt)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private CustomerOrderStats stats(String key, long orderCount, String totalSpent) {
        return new CustomerOrderStats(customerId(key), orderCount, new BigDecimal(totalSpent), NOW);
    }

    private CustomerOrderStats noStats(String key) {
        return new CustomerOrderStats(customerId(key), 0L, BigDecimal.ZERO.setScale(2), null);
    }
}
