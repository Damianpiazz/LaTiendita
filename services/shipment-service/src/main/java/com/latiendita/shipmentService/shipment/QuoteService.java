package com.latiendita.shipmentService.shipment;

import com.latiendita.shipmentService.shipment.dto.QuoteRequest;
import com.latiendita.shipmentService.shipment.dto.QuoteResponse;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private final BigDecimal flatRate;
    private final int minBusinessDays;
    private final int maxBusinessDays;
    private final String currency;

    public QuoteService(@Value("${shipment.flat-rate:2499}") BigDecimal flatRate,
                        @Value("${shipment.eta.min-business-days:3}") int minBusinessDays,
                        @Value("${shipment.eta.max-business-days:5}") int maxBusinessDays,
                        @Value("${shipment.default-currency:ARS}") String currency) {
        this.flatRate = flatRate;
        this.minBusinessDays = minBusinessDays;
        this.maxBusinessDays = maxBusinessDays;
        this.currency = currency;
    }

    public QuoteResponse quote(QuoteRequest request) {
        return quote(request, LocalDate.now());
    }

    public QuoteResponse quote(QuoteRequest request, LocalDate from) {
        return new QuoteResponse(flatRate, currency,
                plusBusinessDays(from, minBusinessDays),
                plusBusinessDays(from, maxBusinessDays));
    }

    private LocalDate plusBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int added = 0;
        while (added < days) {
            result = result.plusDays(1);
            if (!isWeekend(result)) {
                added++;
            }
        }
        return result;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
