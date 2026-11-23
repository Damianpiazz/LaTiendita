package com.latiendita.offerService.seeder;

import com.latiendita.offerService.seeder.model.SeedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orquesta la siembra de promociones, usos, beneficios y eventos outbox. */
@Service
public class DatabaseSeederService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);
    static final String VERSION = "offer-v1";

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final OfferDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 OfferDataGenerator generator, SeederJdbcRepository repository,
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
                SeedExecution skipped = SeedExecution.skipped("offer", VERSION);
                log.info("seed.skipped service=offer version={} reason=already_seeded completed=true",
                        VERSION);
                metrics.record(skipped);
                return skipped;
            }

            SeedDataset dataset = generator.generate();
            if (properties.isCleanBeforeRun()) {
                repository.cleanSeededRows();
                log.info("seed.cleaned service=offer version={} scope=synthetic_only completed=true",
                        VERSION);
            }

            repository.insertPromotions(dataset.promotions());
            repository.insertUsages(dataset.usages());
            repository.insertBenefits(dataset.benefits());
            repository.insertEvents(dataset.events());
            validator.validate(dataset, VERSION);

            long rows = (long) dataset.promotions().size() + dataset.usages().size()
                    + dataset.benefits().size() + dataset.events().size();
            repository.writeMarker(VERSION, rows,
                    "seeder offer-service: " + dataset.promotions().size() + " promotions, "
                            + dataset.usages().size() + " usages, "
                            + dataset.benefits().size() + " benefits, "
                            + dataset.events().size() + " events");
            validator.validateMarker(VERSION);

            SeedExecution execution = SeedExecution.completed("offer", VERSION,
                    dataset.promotions().size(), dataset.usages().size(),
                    dataset.benefits().size(), dataset.events().size(),
                    System.currentTimeMillis() - started);
            log.info("seed.completed service=offer version={} promotions={} usages={} "
                            + "benefits={} outbox={} durationMs={} outcome={} completed=true",
                    VERSION, execution.promotions(), execution.usages(), execution.benefits(),
                    execution.outbox(), execution.durationMs(), execution.outcome());
            metrics.record(execution);
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service=offer version={} error={} completed=false",
                    VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }
}
