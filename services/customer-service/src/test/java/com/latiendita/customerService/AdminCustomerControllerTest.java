package com.latiendita.customerService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.customerService.customer.AdminCustomerController;
import com.latiendita.customerService.customer.AdminCustomerService;
import com.latiendita.customerService.customer.CustomerController;
import com.latiendita.customerService.customer.CustomerService;
import com.latiendita.customerService.customer.CustomerStatus;
import com.latiendita.customerService.customer.dto.AdminCustomerResponse;
import com.latiendita.customerService.customer.dto.AdminCustomerSummaryResponse;
import com.latiendita.customerService.customer.dto.CustomerSegment;
import com.latiendita.customerService.customer.dto.CustomerSummaryDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminCustomerControllerTest {

    private static final Instant NOW = Instant.parse("2026-11-19T15:30:00Z");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AdminCustomerService adminCustomerService;

    @Mock
    private CustomerService customerService;

    private MockMvc adminMvc;
    private MockMvc legacyMvc;

    @BeforeEach
    void setUp() {
        adminMvc = MockMvcBuilders.standaloneSetup(new AdminCustomerController(adminCustomerService))
                .build();
        legacyMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldExposeAdminListingAtTheFrozenPathAndShape() throws Exception {
        when(adminCustomerService.list(isNull(), isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row()), PageRequest.of(0, 20), 1));

        adminMvc.perform(get("/api/customers/admin")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].customerId").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.content[0].firstName").value("Ana"))
                .andExpect(jsonPath("$.content[0].phone").value("555-1000"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].orderCount").value(2))
                .andExpect(jsonPath("$.content[0].totalSpent").value(150.00))
                .andExpect(jsonPath("$.content[0].lastPurchaseAt").value(NOW.toString()))
                .andExpect(jsonPath("$.content[0].segment").value("recurrente"));
    }

    @Test
    void shouldExposeAdminSummaryAtTheFrozenPathAndShape() throws Exception {
        when(adminCustomerService.summary(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new AdminCustomerSummaryResponse(1L, 1L, 2L, new BigDecimal("75.00")));

        adminMvc.perform(get("/api/customers/admin/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.newThisMonth").value(1))
                .andExpect(jsonPath("$.recurring").value(2))
                .andExpect(jsonPath("$.averageTicket").value(75.00));
    }

    @Test
    void shouldForwardPhoneFilterToAllAdminEndpoints() throws Exception {
        when(adminCustomerService.list(isNull(), isNull(), eq("555-1000"), isNull(), isNull(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row()), PageRequest.of(0, 20), 1));
        when(adminCustomerService.summary(isNull(), isNull(), eq("555-1000"), isNull(), isNull()))
                .thenReturn(new AdminCustomerSummaryResponse(1L, 1L, 1L, new BigDecimal("50.00")));
        when(adminCustomerService.exportCsv(isNull(), isNull(), eq("555-1000"), isNull(), isNull(),
                eq("lastPurchaseAt,desc")))
                .thenReturn("customerId,phone\n" + CUSTOMER_ID + ",555-1000\n");

        adminMvc.perform(get("/api/customers/admin").param("phone", "555-1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].phone").value("555-1000"));
        adminMvc.perform(get("/api/customers/admin/summary").param("phone", "555-1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(1));
        adminMvc.perform(get("/api/customers/admin/export.csv").param("phone", "555-1000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("555-1000")));

        verify(adminCustomerService).list(isNull(), isNull(), eq("555-1000"), isNull(), isNull(),
                any(Pageable.class));
        verify(adminCustomerService).summary(isNull(), isNull(), eq("555-1000"), isNull(),
                isNull());
        verify(adminCustomerService).exportCsv(isNull(), isNull(), eq("555-1000"), isNull(),
                isNull(), eq("lastPurchaseAt,desc"));
    }

    @Test
    void shouldExportCsvWithTextCsvContentType() throws Exception {
        when(adminCustomerService.exportCsv(isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("lastPurchaseAt,desc")))
                .thenReturn("customerId,firstName,segment\n" + CUSTOMER_ID + ",Ana,recurrente\n");

        adminMvc.perform(get("/api/customers/admin/export.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition",
                        containsString("admin-customers.csv")))
                .andExpect(content().string(containsString("customerId,firstName,segment")))
                .andExpect(content().string(containsString("recurrente")));
    }

    @Test
    void shouldKeepLegacyCustomerListingUnchanged() throws Exception {
        when(customerService.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new CustomerSummaryDto(
                        CUSTOMER_ID, "Ana", "Diaz", "ana@mail.com", CustomerStatus.ACTIVE, NOW)),
                        PageRequest.of(0, 20), 1));

        legacyMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.content[0].firstName").value("Ana"))
                .andExpect(jsonPath("$.content[0].lastName").value("Diaz"))
                .andExpect(jsonPath("$.content[0].email").value("ana@mail.com"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].lastLoginAt").value(NOW.toString()))
                .andExpect(jsonPath("$.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.content[0].segment").doesNotExist());

        verify(customerService).search(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    private AdminCustomerResponse row() {
        return new AdminCustomerResponse(
                CUSTOMER_ID.toString(), "Ana", "Diaz", "ana@mail.com", "555-1000",
                CustomerStatus.ACTIVE, NOW.minusSeconds(3600), NOW,
                2L, new BigDecimal("150.00"), NOW, CustomerSegment.RECURRENTE);
    }
}
