package com.latiendita.orderService.seeder;

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

    private static final String SERVICE = "order";

    private final Counter ordersCounter;
    private final Counter itemsCounter;
    private final Counter eventsCounter;
    private final Counter skippedCounter;
    private final Counter failureCounter;
    private final Timer durationTimer;
    private final AtomicLong lastSuccess = new AtomicLong();

    public SeederMetrics(MeterRegistry registry) {
        ordersCounter = Counter.builder("seeder.rows")
                .description("Filas insertadas por el seeder")
                .tag("service", SERVICE).tag("entity", "orders")
                .register(registry);
        itemsCounter = Counter.builder("seeder.rows")
                .description("Filas insertadas por el seeder")
                .tag("service", SERVICE).tag("entity", "items")
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
        ordersCounter.increment(execution.orders());
        itemsCounter.increment(execution.items());
        eventsCounter.increment(execution.events());
        durationTimer.record(Duration.ofMillis(execution.durationMs()));
        lastSuccess.set(1L);
    }

    public void recordFailure() {
        failureCounter.increment();
        lastSuccess.set(0L);
    }
}
