package com.latiendita.shipmentService.shipment;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.shipmentService.shipment.dto.QuoteRequest;
import com.latiendita.shipmentService.shipment.dto.QuoteResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class QuoteServiceTest {

    private final QuoteService quoteService =
            new QuoteService(new BigDecimal("2499"), 3, 5, "ARS");

    @Test
    void shouldReturnFlatRateRegardlessOfSubtotal() {
        QuoteResponse cheap = quoteService.quote(new QuoteRequest(BigDecimal.ZERO));
        QuoteResponse expensive = quoteService.quote(new QuoteRequest(new BigDecimal("999999.99")));

        assertThat(cheap.shippingCost()).isEqualByComparingTo("2499");
        assertThat(expensive.shippingCost()).isEqualByComparingTo("2499");
        assertThat(cheap.currency()).isEqualTo("ARS");
    }

    @Test
    void shouldComputeThreeToFiveBusinessDaysSkippingWeekendFromMonday() {
        QuoteResponse response = quoteService.quote(new QuoteRequest(BigDecimal.TEN),
                LocalDate.of(2026, 10, 12));

        assertThat(response.estimatedFrom()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(response.estimatedTo()).isEqualTo(LocalDate.of(2026, 10, 19));
    }

    @Test
    void shouldComputeWindowAcrossWeekendFromFriday() {
        QuoteResponse response = quoteService.quote(new QuoteRequest(BigDecimal.TEN),
                LocalDate.of(2026, 10, 16));

        assertThat(response.estimatedFrom()).isEqualTo(LocalDate.of(2026, 10, 21));
        assertThat(response.estimatedTo()).isEqualTo(LocalDate.of(2026, 10, 23));
    }
}
