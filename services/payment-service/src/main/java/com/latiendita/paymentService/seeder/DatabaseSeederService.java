package com.latiendita.paymentService.seeder;

import com.latiendita.paymentService.seeder.model.SeedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orquesta la siembra de `payments`, `payment_events` y `outbox_events`. */
@Service
public class DatabaseSeederService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);
    static final String VERSION = "payment-v1";

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final PaymentDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 PaymentDataGenerator generator, SeederJdbcRepository repository,
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
                SeedExecution skipped = SeedExecution.skipped("payment", VERSION);
                log.info("seed.skipped service=payment version={} reason=already_seeded "
                        + "completed=true", VERSION);
                metrics.record(skipped);
                return skipped;
            }

            if (properties.isCleanBeforeRun()) {
                repository.cleanSeededRows();
                log.info("seed.cleaned service=payment version={} scope=synthetic_only "
                        + "completed=true", VERSION);
            }

            SeedDataset dataset = generator.generate();
            repository.insertPayments(dataset.payments());
            repository.insertEvents(dataset.events());
            if (properties.isIncludeInfra()) {
                repository.replaceSeededOutbox(properties.getExternalIdPrefix(),
                        dataset.outbox());
            }
            validator.validate(dataset, VERSION);
            repository.writeMarker(VERSION, dataset.payments().size() + dataset.events().size()
                    + dataset.outbox().size(), "seeder payment-service: "
                    + dataset.payments().size() + " payments, " + dataset.events().size()
                    + " events, " + dataset.outbox().size() + " outbox");
            validator.validateMarker(VERSION);

            SeedExecution execution = SeedExecution.completed("payment", VERSION,
                    dataset.payments().size(), dataset.events().size(), dataset.outbox().size(),
                    System.currentTimeMillis() - started);
            log.info("seed.completed service=payment version={} payments={} events={} "
                            + "outbox={} durationMs={} outcome={} completed=true",
                    VERSION, execution.payments(), execution.events(), execution.outbox(),
                    execution.durationMs(), execution.outcome());
            metrics.record(execution);
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service=payment version={} error={} completed=false",
                    VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }
}
