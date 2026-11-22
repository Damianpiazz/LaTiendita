package com.latiendita.orderService.order;

import com.latiendita.orderService.CurrentUser;
import com.latiendita.orderService.order.dto.AdminCustomerOrderStatsResponse;
import com.latiendita.orderService.order.dto.AdminCustomerOrderSummaryResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates that remain owned by order-service and are composed by customer-service. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomerOrderService {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2);

    private final OrderRepository orderRepository;

    public List<AdminCustomerOrderStatsResponse> statsForCustomers(List<String> customerIds) {
        CurrentUser.requireAdmin();
        List<String> ids = distinctIds(customerIds);
        if (ids.isEmpty()) {
            return List.of();
        }

        Specification<OrderEntity> spec = (root, query, cb) -> cb.and(
                root.get("customerId").in(ids),
                cb.notEqual(root.get("status"), OrderStatus.CANCELLED));
        Map<String, Aggregate> byCustomer = aggregateByCustomer(
                orderRepository.findAll(spec).stream()
                        .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                        .filter(order -> ids.contains(order.getCustomerId()))
                        .toList());

        return ids.stream()
                .map(id -> toStats(id, byCustomer.get(id)))
                .toList();
    }

    public AdminCustomerOrderSummaryResponse summaryForCustomers() {
        CurrentUser.requireAdmin();
        Map<String, Aggregate> byCustomer = aggregateByCustomer(
                orderRepository.findAll().stream()
                        .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                        .toList());

        long recurringCustomers = byCustomer.values().stream()
                .filter(aggregate -> aggregate.orderCount >= 2)
                .count();
        BigDecimal totalSpent = byCustomer.values().stream()
                .map(aggregate -> aggregate.totalSpent)
                .reduce(ZERO_MONEY, BigDecimal::add);
        BigDecimal averageTicket = byCustomer.isEmpty()
                ? ZERO_MONEY
                : totalSpent.divide(BigDecimal.valueOf(byCustomer.size()), 2, RoundingMode.HALF_UP);

        return new AdminCustomerOrderSummaryResponse(recurringCustomers, averageTicket);
    }

    private static List<String> distinctIds(List<String> customerIds) {
        if (customerIds == null) {
            return List.of();
        }
        return customerIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static Map<String, Aggregate> aggregateByCustomer(List<OrderEntity> orders) {
        Map<String, Aggregate> byCustomer = new HashMap<>();
        for (OrderEntity order : orders) {
            byCustomer.computeIfAbsent(order.getCustomerId(), ignored -> new Aggregate()).add(order);
        }
        return byCustomer;
    }

    private static AdminCustomerOrderStatsResponse toStats(String customerId, Aggregate aggregate) {
        if (aggregate == null) {
            return new AdminCustomerOrderStatsResponse(customerId, 0L, ZERO_MONEY, null);
        }
        return new AdminCustomerOrderStatsResponse(
                customerId, aggregate.orderCount, aggregate.totalSpent, aggregate.lastPurchaseAt);
    }

    private static final class Aggregate {
        private long orderCount;
        private BigDecimal totalSpent = ZERO_MONEY;
        private Instant lastPurchaseAt;

        private void add(OrderEntity order) {
            orderCount++;
            totalSpent = totalSpent.add(order.getTotal());
            Instant createdAt = order.getCreatedAt();
            if (createdAt != null && (lastPurchaseAt == null || createdAt.isAfter(lastPurchaseAt))) {
                lastPurchaseAt = createdAt;
            }
        }
    }
}
