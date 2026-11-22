package com.latiendita.customerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.latiendita.customerService.customer.AdminCustomerService;
import com.latiendita.customerService.customer.CustomerEntity;
import com.latiendita.customerService.customer.CustomerRepository;
import com.latiendita.customerService.customer.CustomerStatus;
import com.latiendita.customerService.customer.dto.AdminCustomerResponse;
import com.latiendita.customerService.customer.dto.CustomerSegment;
import com.latiendita.customerService.customer.order.CustomerOrderStats;
import com.latiendita.customerService.customer.order.OrderClient;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class T3IndependentClientsProbeTest {
    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");
    private static final String ID = "10000000-0000-0000-0000-000000000001";

    @Mock private CustomerRepository customerRepository;
    @Mock private OrderClient orderClient;
    @Spy private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    @InjectMocks private AdminCustomerService service;

    @BeforeEach
    void feed() {
        when(customerRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(
                customer(CustomerStatus.ACTIVE, NOW.minus(Duration.ofDays(10))),
                otherCustomer(CustomerStatus.SUSPENDED, NOW.minus(Duration.ofDays(60)))));
        when(orderClient.stats(anyList())).thenReturn(List.of(
                new CustomerOrderStats(ID, 1L, new BigDecimal("50.00"), NOW)));
    }

    @Test
    void freeTextSearchTypedOnceMustFindMatchingCustomer() {
        var rows = service.list("ana", "ana", "ana", null, null, PageRequest.of(0, 8));
        System.out.println("T3_PROBE freeTextRows=" + rows.getTotalElements());
        assertThat(rows.getContent()).extracting(AdminCustomerResponse::customerId).containsExactly(ID);
    }

    @Test
    void sinComprasPrecedesSuspendedAndStaysADistinctFilter() {
        when(orderClient.stats(anyList())).thenReturn(List.of());
        var all = service.list(null, null, null, null, null, PageRequest.of(0, 8));
        assertThat(all.getContent()).extracting(AdminCustomerResponse::segment)
                .containsExactly(CustomerSegment.SIN_COMPRAS, CustomerSegment.SIN_COMPRAS);
        assertThat(service.list(null, null, null, null, "sin-compras", PageRequest.of(0, 8))
                .getContent()).hasSize(2);
        assertThat(service.list(null, null, null, null, "inactiva", PageRequest.of(0, 8))
                .getContent()).isEmpty();
    }

    @Test
    void exactThirtyDayBoundaryIsNotNew() {
        when(customerRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(
                active(NOW.minus(Duration.ofDays(30)).plusMillis(1)),
                active(NOW.minus(Duration.ofDays(30)))));
        var rows = service.list(null, null, null, null, null, PageRequest.of(0, 8));
        assertThat(rows.getContent()).extracting(AdminCustomerResponse::segment)
                .containsExactly(CustomerSegment.NUEVA, CustomerSegment.ACTIVA);
    }

    @Test
    void unavailableOrderServiceDegradesToZerosWithoutFabricatingOrders() {
        when(orderClient.stats(anyList())).thenReturn(List.of());
        when(orderClient.summary()).thenReturn(new com.latiendita.customerService.customer.order.CustomerOrderSummary(
                0L, new BigDecimal("0.00")));
        var rows = service.list(null, null, null, null, null, PageRequest.of(0, 8));
        assertThat(rows.getContent()).allSatisfy(row -> {
            assertThat(row.orderCount()).isZero();
            assertThat(row.totalSpent()).isEqualByComparingTo("0.00");
            assertThat(row.lastPurchaseAt()).isNull();
            assertThat(row.segment()).isEqualTo(CustomerSegment.SIN_COMPRAS);
        });
        var summary = service.summary(null, null, null, null, null);
        assertThat(summary.recurring()).isZero();
        assertThat(summary.averageTicket()).isEqualByComparingTo("0.00");
        assertThat(summary.totalCustomers()).isEqualTo(2L);
    }

    @Test
    void emptyRepositoryDoesNotCallOrderServiceAndCsvKeepsHeaderOnly() {
        when(customerRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());
        assertThat(service.list(null, null, null, null, null, PageRequest.of(0, 8)).getContent()).isEmpty();
        assertThat(service.summary(null, null, null, null, null).totalCustomers()).isZero();
        String csv = service.exportCsv(null, null, null, null, null, null);
        assertThat(csv.lines()).hasSize(1);
        assertThat(csv).contains("customerId", "segment", "lastPurchaseAt");
        org.mockito.Mockito.verify(orderClient, org.mockito.Mockito.never()).stats(anyList());
    }

    @Test
    void duplicateStatsAreMergedAndCsvEscapesCommas() {
        when(orderClient.stats(anyList())).thenReturn(List.of(
                new CustomerOrderStats(ID, 2L, new BigDecimal("70.00"), NOW),
                new CustomerOrderStats(ID, 9L, new BigDecimal("999.00"), NOW)));
        var rows = service.list(null, null, null, null, null, PageRequest.of(0, 8));
        assertThat(rows.getContent().get(0).orderCount()).isEqualTo(2L);
        String csv = service.exportCsv(null, null, null, null, null, "orderCount,desc");
        assertThat(csv.lines()).hasSize(3);
    }

    @Test
    void pageBeyondTheLastRowIsEmptyButTotalIsPreserved() {
        var rows = service.list(null, null, null, null, null, PageRequest.of(9, 8));
        assertThat(rows.getContent()).isEmpty();
        assertThat(rows.getTotalElements()).isEqualTo(2L);
    }

    private CustomerEntity active(Instant createdAt) {
        return customer(CustomerStatus.ACTIVE, createdAt);
    }

    private CustomerEntity otherCustomer(CustomerStatus status, Instant createdAt) {
        return CustomerEntity.builder()
                .id(UUID.fromString("20000000-0000-0000-0000-000000000002"))
                .authorizerSub("sub-probe-other")
                .email("beto@mail.com")
                .firstName("Beto")
                .lastName("Ruiz")
                .phone("555-9000")
                .status(status)
                .newsletterOptIn(false)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .lastLoginAt(createdAt)
                .build();
    }

    private CustomerEntity customer(CustomerStatus status, Instant createdAt) {
        return CustomerEntity.builder()
                .id(UUID.fromString(ID))
                .authorizerSub("sub-probe")
                .email("ana@mail.com")
                .firstName("Ana")
                .lastName("Torres")
                .phone("555-1038")
                .status(status)
                .newsletterOptIn(false)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .lastLoginAt(createdAt)
                .build();
    }
}
