package com.latiendita.orderService.order;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.orderService.exception.AdminRoleRequiredException;
import com.latiendita.orderService.exception.GlobalExceptionHandler;
import com.latiendita.orderService.order.dto.AdminCustomerOrderStatsResponse;
import com.latiendita.orderService.order.dto.AdminCustomerOrderSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminCustomerOrderControllerTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:30:00Z");

    @Mock
    private AdminCustomerOrderService adminCustomerOrderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminCustomerOrderController(adminCustomerOrderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldExposePerCustomerStatsAtTheFrozenPathAndShape() throws Exception {
        when(adminCustomerOrderService.statsForCustomers(List.of("c1", "c2"))).thenReturn(List.of(
                new AdminCustomerOrderStatsResponse("c1", 2L, new BigDecimal("150.00"), NOW),
                new AdminCustomerOrderStatsResponse("c2", 0L, BigDecimal.ZERO.setScale(2), null)));

        mockMvc.perform(get("/api/orders/admin/customers/stats")
                        .param("customerIds", "c1,c2")
                        .header("X-User-Roles", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("c1"))
                .andExpect(jsonPath("$[0].orderCount").value(2))
                .andExpect(jsonPath("$[0].totalSpent").value(150.00))
                .andExpect(jsonPath("$[0].lastPurchaseAt").value("2026-11-19T15:30:00Z"))
                .andExpect(jsonPath("$[1].orderCount").value(0))
                .andExpect(jsonPath("$[1].lastPurchaseAt").doesNotExist());
    }

    @Test
    void shouldExposeCustomerSummaryAtTheFrozenPathAndShape() throws Exception {
        when(adminCustomerOrderService.summaryForCustomers())
                .thenReturn(new AdminCustomerOrderSummaryResponse(3L, new BigDecimal("125.00")));

        mockMvc.perform(get("/api/orders/admin/customers/summary")
                        .header("X-User-Roles", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recurringCustomers").value(3))
                .andExpect(jsonPath("$.averageTicket").value(125.00));
    }

    @Test
    void shouldReturnForbiddenProblemDetailWhenAdminRoleIsMissing() throws Exception {
        when(adminCustomerOrderService.summaryForCustomers())
                .thenThrow(new AdminRoleRequiredException());

        mockMvc.perform(get("/api/orders/admin/customers/summary"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"))
                .andExpect(jsonPath("$.detail").value("Se requiere el rol 'admin' para esta operación"));
    }
}
