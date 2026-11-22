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
class AdminCustomerOrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:30:00Z");

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminCustomerOrderService adminCustomerOrderService;

    @BeforeEach
    void setAdminUser() {
        setHeaders("admin-1", "cliente,admin");
    }

    @AfterEach
    void resetCurrentUser() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldExcludeCancelledOrdersFromPerCustomerStats() {
        when(orderRepository.findAll(any(Specification.class))).thenReturn(List.of(
                order("c1", OrderStatus.PAID, "100.00", NOW.minusSeconds(3600)),
                order("c1", OrderStatus.DELIVERED, "50.00", NOW),
                order("c1", OrderStatus.CANCELLED, "999.00", NOW.plusSeconds(60)),
                order("c2", OrderStatus.CANCELLED, "777.00", NOW)));

        var stats = adminCustomerOrderService.statsForCustomers(List.of("c1", "c2", "c3"));

        assertThat(stats).hasSize(3);
        assertThat(stats.get(0).customerId()).isEqualTo("c1");
        assertThat(stats.get(0).orderCount()).isEqualTo(2L);
        assertThat(stats.get(0).totalSpent()).isEqualByComparingTo("150.00");
        assertThat(stats.get(0).lastPurchaseAt()).isEqualTo(NOW);
        assertThat(stats.get(1).customerId()).isEqualTo("c2");
        assertThat(stats.get(1).orderCount()).isZero();
        assertThat(stats.get(1).totalSpent()).isEqualByComparingTo("0.00");
        assertThat(stats.get(1).lastPurchaseAt()).isNull();
        assertThat(stats.get(2).customerId()).isEqualTo("c3");
        assertThat(stats.get(2).orderCount()).isZero();
        assertThat(stats.get(2).totalSpent()).isEqualByComparingTo("0.00");
        assertThat(stats.get(2).lastPurchaseAt()).isNull();
    }

    @Test
    void shouldComputeRecurringAndAverageTicketExcludingCancelled() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("c1", OrderStatus.PAID, "100.00", NOW),
                order("c1", OrderStatus.PAID, "40.00", NOW),
                order("c2", OrderStatus.DELIVERED, "60.00", NOW),
                order("c3", OrderStatus.CANCELLED, "999.00", NOW)));

        var summary = adminCustomerOrderService.summaryForCustomers();

        assertThat(summary.recurringCustomers()).isEqualTo(1L);
        assertThat(summary.averageTicket()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldReturnZeroAverageTicketWhenNobodyPurchased() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order("c1", OrderStatus.CANCELLED, "999.00", NOW)));

        var summary = adminCustomerOrderService.summaryForCustomers();

        assertThat(summary.recurringCustomers()).isZero();
        assertThat(summary.averageTicket()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldRequireAdminRoleForCustomerStats() {
        setHeaders("customer-1", "cliente");

        assertThatThrownBy(() -> adminCustomerOrderService.statsForCustomers(List.of("c1")))
                .isInstanceOf(AdminRoleRequiredException.class);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldRequireAdminRoleForCustomerSummary() {
        setHeaders("customer-1", null);

        assertThatThrownBy(() -> adminCustomerOrderService.summaryForCustomers())
                .isInstanceOf(AdminRoleRequiredException.class);
        verifyNoInteractions(orderRepository);
    }

    private void setHeaders(String userId, String roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", userId);
        if (roles != null) {
            request.addHeader("X-User-Roles", roles);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private OrderEntity order(String customerId, OrderStatus status, String total, Instant createdAt) {
        return OrderEntity.builder()
                .orderNumber("ORD-" + customerId)
                .customerId(customerId)
                .status(status)
                .total(new BigDecimal(total))
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
