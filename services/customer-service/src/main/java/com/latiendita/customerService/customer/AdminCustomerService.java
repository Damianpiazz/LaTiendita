package com.latiendita.customerService.customer;

import com.latiendita.customerService.customer.dto.AdminCustomerResponse;
import com.latiendita.customerService.customer.dto.AdminCustomerSummaryResponse;
import com.latiendita.customerService.customer.dto.CustomerSegment;
import com.latiendita.customerService.customer.order.CustomerOrderStats;
import com.latiendita.customerService.customer.order.CustomerOrderSummary;
import com.latiendita.customerService.customer.order.OrderClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Composes customer-owned data with order-owned aggregates for Admin Clientes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomerService {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2);
    private static final Duration NEW_CUSTOMER_WINDOW = Duration.ofDays(30);
    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "customerId", "firstName", "lastName", "email", "phone", "status",
            "createdAt", "lastLoginAt", "orderCount", "totalSpent", "lastPurchaseAt", "segment");
    private static final String CSV_HEADER =
            "customerId,firstName,lastName,email,phone,status,createdAt,lastLoginAt,"
                    + "orderCount,totalSpent,lastPurchaseAt,segment";

    private final CustomerRepository customerRepository;
    private final OrderClient orderClient;
    private final Clock clock;

    public Page<AdminCustomerResponse> list(String name, String email, String phone,
                                            CustomerStatus status, String segment,
                                            Pageable pageable) {
        List<AdminCustomerResponse> rows = buildRows(name, email, phone, status, segment);
        rows.sort(comparator(pageable.getSort()));

        int total = rows.size();
        int from = (int) Math.min(pageable.getOffset(), total);
        int to = Math.min(from + pageable.getPageSize(), total);
        return new PageImpl<>(new ArrayList<>(rows.subList(from, to)), pageable, total);
    }

    public AdminCustomerSummaryResponse summary(String name, String email, String phone,
                                                CustomerStatus status, String segment) {
        List<AdminCustomerResponse> rows = buildRows(name, email, phone, status, segment);
        Instant monthStart = LocalDate.now(clock)
                .withDayOfMonth(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        long newThisMonth = rows.stream()
                .filter(row -> row.createdAt() != null && !row.createdAt().isBefore(monthStart))
                .count();

        boolean unfiltered = !hasText(name) && !hasText(email) && !hasText(phone)
                && status == null && !hasText(segment);
        long recurring;
        BigDecimal averageTicket;
        if (unfiltered) {
            CustomerOrderSummary orderSummary = orderClient.summary();
            recurring = orderSummary == null ? 0L : orderSummary.recurringCustomers();
            averageTicket = orderSummary == null || orderSummary.averageTicket() == null
                    ? ZERO_MONEY
                    : orderSummary.averageTicket();
        } else {
            List<String> ids = rows.stream().map(AdminCustomerResponse::customerId).toList();
            List<CustomerOrderStats> stats = ids.isEmpty() ? List.of() : orderClient.stats(ids);
            recurring = stats.stream().filter(stat -> stat.orderCount() >= 2).count();
            BigDecimal spent = stats.stream()
                    .map(stat -> stat.totalSpent() == null ? ZERO_MONEY : stat.totalSpent())
                    .reduce(ZERO_MONEY, BigDecimal::add);
            long withOrders = stats.stream().filter(stat -> stat.orderCount() > 0).count();
            averageTicket = withOrders == 0
                    ? ZERO_MONEY
                    : spent.divide(BigDecimal.valueOf(withOrders), 2, RoundingMode.HALF_UP);
        }

        return new AdminCustomerSummaryResponse(rows.size(), newThisMonth, recurring, averageTicket);
    }

    public String exportCsv(String name, String email, String phone, CustomerStatus status,
                            String segment, String sort) {
        List<AdminCustomerResponse> rows = buildRows(name, email, phone, status, segment);
        rows.sort(comparator(parseSort(sort)));

        StringBuilder csv = new StringBuilder(CSV_HEADER);
        for (AdminCustomerResponse row : rows) {
            csv.append('\n')
                    .append(csv(row.customerId())).append(',')
                    .append(csv(row.firstName())).append(',')
                    .append(csv(row.lastName())).append(',')
                    .append(csv(row.email())).append(',')
                    .append(csv(row.phone())).append(',')
                    .append(csv(row.status() == null ? null : row.status().name())).append(',')
                    .append(csv(row.createdAt() == null ? null : row.createdAt().toString())).append(',')
                    .append(csv(row.lastLoginAt() == null ? null : row.lastLoginAt().toString())).append(',')
                    .append(row.orderCount()).append(',')
                    .append(csv(row.totalSpent())).append(',')
                    .append(csv(row.lastPurchaseAt() == null ? null : row.lastPurchaseAt().toString()))
                    .append(',')
                    .append(csv(row.segment() == null ? null : row.segment().wireValue()));
        }
        return csv.toString();
    }

    private List<AdminCustomerResponse> buildRows(String name, String email, String phone,
                                                  CustomerStatus status, String segment) {
        List<CustomerEntity> customers = customerRepository.findAllByDeletedAtIsNull();
        if (customers.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> ids = customers.stream()
                .map(customer -> customer.getId().toString())
                .toList();
        List<CustomerOrderStats> stats = orderClient.stats(ids);
        Map<String, CustomerOrderStats> statsById = stats == null
                ? Map.of()
                : stats.stream().collect(Collectors.toMap(
                        CustomerOrderStats::customerId, Function.identity(), (first, ignored) -> first));

        Instant now = clock.instant();
        List<AdminCustomerResponse> rows = new ArrayList<>();
        for (CustomerEntity customer : customers) {
            AdminCustomerResponse row = toResponse(
                    customer, statsById.get(customer.getId().toString()), now);
            if (matches(row, name, email, phone, status, segment)) {
                rows.add(row);
            }
        }
        return rows;
    }

    private static AdminCustomerResponse toResponse(CustomerEntity customer,
                                                    CustomerOrderStats stats, Instant now) {
        long orderCount = stats == null ? 0L : stats.orderCount();
        BigDecimal totalSpent = stats == null || stats.totalSpent() == null
                ? ZERO_MONEY
                : stats.totalSpent();
        Instant lastPurchaseAt = stats == null ? null : stats.lastPurchaseAt();
        return new AdminCustomerResponse(
                customer.getId().toString(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getLastLoginAt(),
                orderCount,
                totalSpent,
                lastPurchaseAt,
                segment(customer, orderCount, now));
    }

    private static CustomerSegment segment(CustomerEntity customer, long orderCount, Instant now) {
        if (orderCount == 0) {
            return CustomerSegment.SIN_COMPRAS;
        }
        if (orderCount >= 2) {
            return CustomerSegment.RECURRENTE;
        }
        Instant createdAt = customer.getCreatedAt();
        if (createdAt != null && createdAt.isAfter(now.minus(NEW_CUSTOMER_WINDOW))) {
            return CustomerSegment.NUEVA;
        }
        return customer.getStatus() == CustomerStatus.SUSPENDED
                ? CustomerSegment.INACTIVA
                : CustomerSegment.ACTIVA;
    }

    private static boolean matches(AdminCustomerResponse row, String name, String email,
                                   String phone, CustomerStatus status, String segment) {
        String nameTerm = hasText(name) ? name.trim().toLowerCase() : null;
        String emailTerm = hasText(email) ? email.trim().toLowerCase() : null;
        String phoneTerm = hasText(phone) ? phone.trim().toLowerCase() : null;

        // The wireframe has a single free-text box and sends the same term in every
        // filter. Treat that repeated term as one OR search instead of three ANDs;
        // distinct email/phone values keep their narrower meaning.
        boolean freeText = nameTerm != null && nameTerm.equals(emailTerm)
                && nameTerm.equals(phoneTerm);
        if (nameTerm != null && !contains(row.firstName(), nameTerm)
                && !contains(row.lastName(), nameTerm)
                && !contains(row.email(), nameTerm)
                && !contains(row.phone(), nameTerm)) {
            return false;
        }
        if (!freeText && emailTerm != null && !contains(row.email(), emailTerm)) {
            return false;
        }
        if (!freeText && phoneTerm != null && !contains(row.phone(), phoneTerm)) {
            return false;
        }
        if (status != null && row.status() != status) {
            return false;
        }
        CustomerSegment requested = CustomerSegment.fromWireValue(segment);
        return !hasText(segment) || (requested != null && row.segment() == requested);
    }

    private static Comparator<AdminCustomerResponse> comparator(Sort sort) {
        Comparator<AdminCustomerResponse> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<AdminCustomerResponse> next = fieldComparator(order.getProperty());
            if (next == null) {
                continue;
            }
            if (order.isDescending()) {
                next = next.reversed();
            }
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        if (comparator == null) {
            return Comparator.comparing(AdminCustomerResponse::createdAt,
                    Comparator.nullsLast(Instant::compareTo)).reversed();
        }
        return comparator;
    }

    private static Comparator<AdminCustomerResponse> fieldComparator(String property) {
        return switch (property) {
            case "customerId" -> text(AdminCustomerResponse::customerId);
            case "firstName" -> text(AdminCustomerResponse::firstName);
            case "lastName" -> text(AdminCustomerResponse::lastName);
            case "email" -> text(AdminCustomerResponse::email);
            case "phone" -> text(AdminCustomerResponse::phone);
            case "status" -> Comparator.comparing(
                    (AdminCustomerResponse row) -> row.status() == null ? null : row.status().name(),
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "createdAt" -> instant(AdminCustomerResponse::createdAt);
            case "lastLoginAt" -> instant(AdminCustomerResponse::lastLoginAt);
            case "lastPurchaseAt" -> instant(AdminCustomerResponse::lastPurchaseAt);
            case "orderCount" -> Comparator.comparingLong(AdminCustomerResponse::orderCount);
            case "totalSpent" -> Comparator.comparing(AdminCustomerResponse::totalSpent,
                    Comparator.nullsLast(BigDecimal::compareTo));
            case "segment" -> Comparator.comparing(
                    (AdminCustomerResponse row) -> row.segment() == null ? null : row.segment().wireValue(),
                    Comparator.nullsLast(String::compareToIgnoreCase));
            default -> null;
        };
    }

    private static Comparator<AdminCustomerResponse> text(
            Function<AdminCustomerResponse, String> extractor) {
        return Comparator.comparing(extractor,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    private static Comparator<AdminCustomerResponse> instant(
            Function<AdminCustomerResponse, Instant> extractor) {
        return Comparator.comparing(extractor, Comparator.nullsLast(Instant::compareTo));
    }

    private static Sort parseSort(String sort) {
        if (!hasText(sort)) {
            return Sort.by(Sort.Order.desc("lastPurchaseAt"));
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (!SORTABLE_FIELDS.contains(property)) {
            property = "lastPurchaseAt";
        }
        Sort.Direction direction = parts.length == 2 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    private static String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains("\"") || text.contains(",") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private static boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
