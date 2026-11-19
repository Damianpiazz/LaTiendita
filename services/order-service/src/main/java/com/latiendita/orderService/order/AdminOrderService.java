package com.latiendita.orderService.order;

import com.latiendita.orderService.CurrentUser;
import com.latiendita.orderService.order.dto.AdminOrderResponse;
import com.latiendita.orderService.order.dto.AdminOrderSummaryResponse;
import com.latiendita.orderService.order.dto.AdminSalesDayResponse;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Global order queries for the admin screens; never exposed without the admin role. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private static final int SUMMARY_DAYS = 7;

    private final OrderRepository orderRepository;
    private final Clock clock;

    public Page<AdminOrderResponse> listForAdmin(OrderStatus status, Instant from, Instant to,
                                                 Pageable pageable) {
        CurrentUser.requireAdmin();
        Specification<OrderEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return orderRepository.findAll(spec, pageable).map(AdminOrderService::toResponse);
    }

    public AdminOrderSummaryResponse summaryForAdmin() {
        CurrentUser.requireAdmin();

        LocalDate today = LocalDate.now(clock);
        Instant weekStart = today.minusDays(SUMMARY_DAYS - 1L)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant tomorrowExclusive = today.plusDays(1L)
                .atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<LocalDate, List<OrderEntity>> byDay = orderRepository
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(weekStart, tomorrowExclusive)
                .stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(order ->
                        order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate()));

        List<AdminSalesDayResponse> salesByDay = new ArrayList<>();
        for (int offset = SUMMARY_DAYS - 1; offset >= 0; offset--) {
            LocalDate day = today.minusDays(offset);
            List<OrderEntity> dayOrders = byDay.getOrDefault(day, List.of());
            BigDecimal total = dayOrders.stream()
                    .map(OrderEntity::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long count = dayOrders.size();
            BigDecimal averageTicket = count == 0
                    ? BigDecimal.ZERO.setScale(2)
                    : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            salesByDay.add(new AdminSalesDayResponse(day, total, count, averageTicket));
        }

        AdminSalesDayResponse todaySales = salesByDay.get(SUMMARY_DAYS - 1);
        long pendingOrders = orderRepository.countByStatusIn(
                List.of(OrderStatus.PENDING, OrderStatus.PAID));
        long pendingWithoutPayment = orderRepository.countByStatusIn(List.of(OrderStatus.PENDING));
        List<AdminOrderResponse> recentOrders = orderRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(AdminOrderService::toResponse)
                .toList();

        return new AdminOrderSummaryResponse(todaySales.total(), todaySales.orders(),
                pendingOrders, pendingWithoutPayment, salesByDay, recentOrders);
    }

    private static AdminOrderResponse toResponse(OrderEntity order) {
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotal(),
                order.getItems().size(),
                order.getCreatedAt());
    }
}
