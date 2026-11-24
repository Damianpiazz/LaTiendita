package com.latiendita.cartService.seeder;

import com.latiendita.cartService.seeder.model.SeedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orquesta la siembra de `carts` y `cart_items`. */
@Service
public class DatabaseSeederService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);
    static final String VERSION = "cart-v1";

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final CartDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 CartDataGenerator generator, SeederJdbcRepository repository,
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
                SeedExecution skipped = SeedExecution.skipped("cart", VERSION);
                log.info("seed.skipped service=cart version={} reason=already_seeded completed=true",
                        VERSION);
                metrics.record(skipped);
                return skipped;
            }

            SeedDataset dataset = generator.generate();
            if (properties.isCleanBeforeRun()) {
                repository.cleanSeededRows(dataset.carts());
                log.info("seed.cleaned service=cart version={} scope=synthetic_only completed=true",
                        VERSION);
            }

            repository.insertCarts(dataset.carts());
            repository.insertItems(dataset.items());
            validator.validate(dataset, VERSION);
            repository.writeMarker(VERSION, dataset.carts().size() + dataset.items().size(),
                    "seeder cart-service: " + dataset.carts().size() + " carts, "
                            + dataset.items().size() + " items");
            validator.validateMarker(VERSION);

            SeedExecution execution = SeedExecution.completed("cart", VERSION,
                    dataset.carts().size(), dataset.items().size(),
                    System.currentTimeMillis() - started);
            log.info("seed.completed service=cart version={} carts={} items={} durationMs={} "
                            + "outcome={} completed=true",
                    VERSION, execution.carts(), execution.items(), execution.durationMs(),
                    execution.outcome());
            metrics.record(execution);
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service=cart version={} error={} completed=false",
                    VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }
}
