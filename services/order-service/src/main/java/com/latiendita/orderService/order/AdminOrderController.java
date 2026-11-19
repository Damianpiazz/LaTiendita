package com.latiendita.orderService.order;

import com.latiendita.orderService.order.dto.AdminOrderResponse;
import com.latiendita.orderService.order.dto.AdminOrderSummaryResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public Page<AdminOrderResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return adminOrderService.listForAdmin(status, from, to, pageable);
    }

    @GetMapping("/summary")
    public AdminOrderSummaryResponse summary() {
        return adminOrderService.summaryForAdmin();
    }
}
