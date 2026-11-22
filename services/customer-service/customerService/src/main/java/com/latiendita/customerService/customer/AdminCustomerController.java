package com.latiendita.customerService.customer;

import com.latiendita.customerService.customer.dto.AdminCustomerResponse;
import com.latiendita.customerService.customer.dto.AdminCustomerSummaryResponse;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin Clientes endpoints. The legacy {@code GET /api/customers} stays in CustomerController. */
@RestController
@RequestMapping("/api/customers/admin")
@RequiredArgsConstructor
public class AdminCustomerController {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "customerId", "firstName", "lastName", "email", "phone", "status",
            "createdAt", "lastLoginAt", "orderCount", "totalSpent", "lastPurchaseAt", "segment");

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    public Page<AdminCustomerResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String segment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastPurchaseAt,desc") String sort) {
        return adminCustomerService.list(name, email, phone, status, segment,
                pageable(page, size, sort));
    }

    @GetMapping("/summary")
    public AdminCustomerSummaryResponse summary(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String segment) {
        return adminCustomerService.summary(name, email, phone, status, segment);
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String segment,
            @RequestParam(defaultValue = "lastPurchaseAt,desc") String sort) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"admin-customers.csv\"")
                .body(adminCustomerService.exportCsv(name, email, phone, status, segment, sort));
    }

    private static Pageable pageable(int page, int size, String sort) {
        String[] parts = sort == null || sort.isBlank()
                ? new String[]{"lastPurchaseAt", "desc"}
                : sort.split(",", 2);
        String property = parts[0].trim();
        if (!SORTABLE_FIELDS.contains(property)) {
            property = "lastPurchaseAt";
        }
        Sort.Direction direction = parts.length == 2 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, property));
    }
}
