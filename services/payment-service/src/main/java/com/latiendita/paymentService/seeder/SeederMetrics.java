package com.latiendita.paymentService.seeder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Métricas del seeder. El éxito sólo se publica tras validar la base de datos. */
@Component
public class SeederMetrics {

    private static final String SERVICE = "payment";

    private final Counter paymentsCounter;
    private final Counter inboxCounter;
    private final Counter eventsCounter;
    private final Counter skippedCounter;
    private final Counter failureCounter;
    private final Timer durationTimer;
    private final AtomicLong lastSuccess = new AtomicLong();

    public SeederMetrics(MeterRegistry registry) {
        paymentsCounter = Counter.builder("seeder.rows")
                .description("Filas insertadas por el seeder")
                .tag("service", SERVICE).tag("entity", "payments")
                .register(registry);
        inboxCounter = Counter.builder("seeder.rows")
                .description("Filas insertadas por el seeder")
                .tag("service", SERVICE).tag("entity", "payment_events")
                .register(registry);
        eventsCounter = Counter.builder("seeder.rows")
                .description("Filas insertadas por el seeder")
                .tag("service", SERVICE).tag("entity", "events")
                .register(registry);
        skippedCounter = Counter.builder("seeder.runs")
                .description("Ejecuciones omitidas por marcador")
                .tag("service", SERVICE).tag("outcome", "skipped")
                .register(registry);
        failureCounter = Counter.builder("seeder.runs")
                .description("Ejecuciones fallidas del seeder")
                .tag("service", SERVICE).tag("outcome", "failed")
                .register(registry);
        durationTimer = Timer.builder("seeder.duration")
                .description("Duración de las ejecuciones del seeder")
                .tag("service", SERVICE)
                .register(registry);
        Gauge.builder("seeder.last.success", lastSuccess, AtomicLong::get)
                .description("1 si la última ejecución verificó la BD, 0 si falló")
                .tag("service", SERVICE)
                .register(registry);
    }

    public void record(SeedExecution execution) {
        if (!execution.executed()) {
            skippedCounter.increment();
            return;
        }
        paymentsCounter.increment(execution.payments());
        inboxCounter.increment(execution.events());
        eventsCounter.increment(execution.outbox());
        durationTimer.record(Duration.ofMillis(execution.durationMs()));
        lastSuccess.set(1L);
    }

    public void recordFailure() {
        failureCounter.increment();
        lastSuccess.set(0L);
    }
}
