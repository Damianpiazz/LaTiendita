package com.latiendita.orderService.order;

import com.latiendita.orderService.order.dto.AdminCustomerOrderStatsResponse;
import com.latiendita.orderService.order.dto.AdminCustomerOrderSummaryResponse;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Order-owned aggregates consumed by customer-service for Admin Clientes. */
@RestController
@RequestMapping("/api/orders/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerOrderController {

    private final AdminCustomerOrderService adminCustomerOrderService;

    @GetMapping("/stats")
    public List<AdminCustomerOrderStatsResponse> stats(
            @RequestParam(name = "customerIds") String customerIds) {
        List<String> ids = Arrays.stream(customerIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
        return adminCustomerOrderService.statsForCustomers(ids);
    }

    @GetMapping("/summary")
    public AdminCustomerOrderSummaryResponse summary() {
        return adminCustomerOrderService.summaryForCustomers();
    }
}
