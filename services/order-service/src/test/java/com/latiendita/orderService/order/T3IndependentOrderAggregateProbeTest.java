package com.latiendita.orderService.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.latiendita.orderService.exception.AdminRoleRequiredException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class T3IndependentOrderAggregateProbeTest {
    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    @Mock private OrderRepository orderRepository;
    @InjectMocks private AdminCustomerOrderService service;

    @BeforeEach
    void admin() {
        headers("admin-1", "cliente,admin");
    }

    @AfterEach
    void reset() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void duplicateIdsYieldOneRowEachAndCancelledOrdersAreIgnored() {
        when(orderRepository.findAll(any(Specification.class))).thenReturn(List.of(
                order("c1", OrderStatus.PAID, "30.00", NOW.minusSeconds(60)),
                order("c1", OrderStatus.DELIVERED, "70.00", NOW),
                order("c1", OrderStatus.CANCELLED, "900.00", NOW.plusSeconds(60)),
                order("c2", OrderStatus.CANCELLED, "800.00", NOW)));
        var stats = service.statsForCustomers(List.of("c1", "c1", "c2", "c3"));
        System.out.println("T3_PROBE statsRows=" + stats.size());
        assertThat(stats).hasSize(3);
        assertThat(stats.get(0).orderCount()).isEqualTo(2L);
        assertThat(stats.get(0).totalSpent()).isEqualByComparingTo("100.00");
        assertThat(stats.get(0).lastPurchaseAt()).isEqualTo(NOW);
        assertThat(stats.get(1).orderCount()).isZero();
        assertThat(stats.get(1).lastPurchaseAt()).isNull();
        assertThat(stats.get(2).customerId()).isEqualTo("c3");
    }

    @Test
    void emptyAndBlankIdsNeverHitTheRepository() {
        assertThat(service.statsForCustomers(List.of())).isEmpty();
        assertThat(service.statsForCustomers(java.util.Arrays.asList("  ", null))).isEmpty();
        verifyNoInteractions(orderRepository);
    }

    @Test
    void summaryDividesByCustomersWithOrdersAndExcludesCancelled() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("c1", OrderStatus.PAID, "100.00", NOW),
                order("c1", OrderStatus.PAID, "40.00", NOW),
                order("c2", OrderStatus.DELIVERED, "60.00", NOW),
                order("c3", OrderStatus.CANCELLED, "999.00", NOW)));
        var summary = service.summaryForCustomers();
        assertThat(summary.recurringCustomers()).isEqualTo(1L);
        assertThat(summary.averageTicket()).isEqualByComparingTo("100.00");
    }

    @Test
    void summaryWithNoRealOrdersIsZeroAndNonAdminIsRejected() {
        when(orderRepository.findAll()).thenReturn(List.of(order("c1", OrderStatus.CANCELLED, "9.00", NOW)));
        assertThat(service.summaryForCustomers().averageTicket()).isEqualByComparingTo("0.00");
        headers("customer-1", "cliente");
        assertThatThrownBy(() -> service.statsForCustomers(List.of("c1")))
                .isInstanceOf(AdminRoleRequiredException.class);
    }

    private void headers(String id, String roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", id);
        if (roles != null) {
            request.addHeader("X-User-Roles", roles);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private OrderEntity order(String customerId, OrderStatus status, String total, Instant createdAt) {
        return OrderEntity.builder()
                .orderNumber("T3-" + customerId + "-" + total)
                .customerId(customerId)
                .status(status)
                .total(new BigDecimal(total))
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
