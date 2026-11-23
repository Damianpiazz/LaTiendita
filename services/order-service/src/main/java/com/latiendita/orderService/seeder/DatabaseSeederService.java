package com.latiendita.orderService.seeder;

import com.latiendita.orderService.seeder.model.SeedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orquesta la siembra de `orders`, `order_items` y `outbox_events`. */
@Service
public class DatabaseSeederService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);
    static final String VERSION = "order-v1";

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final OrderDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 OrderDataGenerator generator, SeederJdbcRepository repository,
                                 SeederValidator validator, SeederMetrics metrics) {
        this.properties = properties;
        this.guard = guard;
        this.generator = generator;
        this.repository = repository;
        this.validator = validator;
        this.metrics = metrics;
    }

    public SeedExecution seed() {
        long started = System.currentTimeMillis();
        try {
            guard.validateClean();
            repository.ensureMarkerTable();

            if (repository.markerExists(VERSION)) {
                SeedExecution skipped = SeedExecution.skipped("order", VERSION);
                log.info("seed.skipped service=order version={} reason=already_seeded completed=true",
                        VERSION);
                metrics.record(skipped);
                return skipped;
            }

            if (properties.isCleanBeforeRun()) {
                repository.cleanSeededRows();
                log.info("seed.cleaned service=order version={} scope=synthetic_only completed=true",
                        VERSION);
            }

            SeedDataset dataset = generator.generate();
            repository.insertOrders(dataset.orders());
            repository.insertItems(dataset.items());
            if (properties.isIncludeInfra()) {
                repository.replaceSeededEvents(properties.getExternalIdPrefix(), dataset.events());
            }
            validator.validate(dataset, VERSION);
            repository.writeMarker(VERSION, dataset.orders().size() + dataset.items().size()
                    + dataset.events().size(), "seeder order-service: "
                    + dataset.orders().size() + " orders, " + dataset.items().size()
                    + " items, " + dataset.events().size() + " events");
            validator.validateMarker(VERSION);

            SeedExecution execution = SeedExecution.completed("order", VERSION,
                    dataset.orders().size(), dataset.items().size(), dataset.events().size(),
                    System.currentTimeMillis() - started);
            log.info("seed.completed service=order version={} orders={} items={} events={} "
                            + "durationMs={} outcome={} completed=true",
                    VERSION, execution.orders(), execution.items(), execution.events(),
                    execution.durationMs(), execution.outcome());
            metrics.record(execution);
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service=order version={} error={} completed=false",
                    VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }
}
