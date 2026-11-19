package com.latiendita.orderService.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.latiendita.orderService.exception.AdminRoleRequiredException;
import com.latiendita.orderService.order.dto.AdminOrderResponse;
import com.latiendita.orderService.order.dto.AdminOrderSummaryResponse;
import com.latiendita.orderService.order.dto.AdminSalesDayResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:30:00Z");
    private static final ZoneOffset UTC = ZoneOffset.UTC;

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private Clock clock = Clock.fixed(NOW, UTC);

    @InjectMocks
    private AdminOrderService adminOrderService;

    @BeforeEach
    void setAdminUser() {
        setHeaders("admin-1", "cliente,admin");
    }

    @AfterEach
    void resetCurrentUser() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldRequireAdminRoleToListOrders() {
        setHeaders("customer-1", "cliente");

        assertThatThrownBy(() -> adminOrderService.listForAdmin(null, null, null,
                PageRequest.of(0, 20)))
                .isInstanceOf(AdminRoleRequiredException.class);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldRequireAdminRoleForSummary() {
        setHeaders("customer-1", null);

        assertThatThrownBy(() -> adminOrderService.summaryForAdmin())
                .isInstanceOf(AdminRoleRequiredException.class);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void shouldListAllCustomersOrdersMappedForAdminTable() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(order("customer-9", OrderStatus.PAID,
                        "120.00", NOW, 2))));

        var page = adminOrderService.listForAdmin(OrderStatus.PAID, null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        AdminOrderResponse row = page.getContent().get(0);
        assertThat(row.customerId()).isEqualTo("customer-9");
        assertThat(row.status()).isEqualTo(OrderStatus.PAID);
        assertThat(row.itemCount()).isEqualTo(2);
        assertThat(row.total()).isEqualByComparingTo("120.00");
    }

    @Test
    void shouldComputeSummaryWithTodaySalesSevenDaySeriesAndPendingCounts() {
        LocalDate today = NOW.atZone(UTC).toLocalDate();
        Instant weekStart = today.minusDays(6).atStartOfDay(UTC).toInstant();
        Instant tomorrow = today.plusDays(1).atStartOfDay(UTC).toInstant();
        when(orderRepository.countByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.PAID)))
                .thenReturn(4L);
        when(orderRepository.countByStatusIn(List.of(OrderStatus.PENDING)))
                .thenReturn(2L);
        when(orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                weekStart, tomorrow)).thenReturn(List.of(
                order("c1", OrderStatus.PAID, "100.00", NOW, 1),
                order("c2", OrderStatus.PENDING, "50.00", NOW, 2),
                order("c3", OrderStatus.CANCELLED, "999.00", NOW, 3),
                order("c4", OrderStatus.DELIVERED, "20.00", weekStart, 1)));
        when(orderRepository.findTop5ByOrderByCreatedAtDesc())
                .thenReturn(List.of(order("c1", OrderStatus.PAID, "100.00", NOW, 1)));

        AdminOrderSummaryResponse summary = adminOrderService.summaryForAdmin();

        assertThat(summary.pendingOrders()).isEqualTo(4L);
        assertThat(summary.pendingWithoutPayment()).isEqualTo(2L);
        assertThat(summary.salesToday()).isEqualByComparingTo("150.00");
        assertThat(summary.ordersToday()).isEqualTo(2L);
        assertThat(summary.salesByDay()).hasSize(7);
        AdminSalesDayResponse first = summary.salesByDay().get(0);
        AdminSalesDayResponse last = summary.salesByDay().get(6);
        assertThat(first.date()).isEqualTo(today.minusDays(6));
        assertThat(first.total()).isEqualByComparingTo("20.00");
        assertThat(first.orders()).isEqualTo(1L);
        assertThat(last.date()).isEqualTo(today);
        assertThat(last.total()).isEqualByComparingTo("150.00");
        assertThat(last.orders()).isEqualTo(2L);
        assertThat(last.averageTicket()).isEqualByComparingTo("75.00");
        assertThat(summary.recentOrders()).hasSize(1);
    }

    private void setHeaders(String userId, String roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", userId);
        if (roles != null) {
            request.addHeader("X-User-Roles", roles);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private OrderEntity order(String customerId, OrderStatus status, String total,
                              Instant createdAt, int items) {
        OrderEntity order = OrderEntity.builder()
                .orderNumber("ORD-" + customerId)
                .customerId(customerId)
                .status(status)
                .total(new BigDecimal(total))
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
        for (int i = 0; i < items; i++) {
            order.addItem(OrderItemEntity.builder()
                    .order(order)
                    .productId(42L + i)
                    .productTitle("Producto " + i)
                    .sku("SKU-" + i)
                    .quantity(1)
                    .unitPrice(new BigDecimal(total))
                    .subtotal(new BigDecimal(total))
                    .build());
        }
        return order;
    }
}
