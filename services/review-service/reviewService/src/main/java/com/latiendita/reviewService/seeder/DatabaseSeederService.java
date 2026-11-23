package com.latiendita.reviewService.seeder;

import com.latiendita.reviewService.seeder.model.SeedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orquesta la siembra de reseñas, votos, agregados y eventos outbox. */
@Service
public class DatabaseSeederService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);
    static final String VERSION = "review-v1";

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final ReviewDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 ReviewDataGenerator generator, SeederJdbcRepository repository,
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
                SeedExecution skipped = SeedExecution.skipped("review", VERSION);
                log.info("seed.skipped service=review version={} reason=already_seeded completed=true",
                        VERSION);
                metrics.record(skipped);
                return skipped;
            }

            SeedDataset dataset = generator.generate();
            if (properties.isCleanBeforeRun()) {
                repository.cleanSeededRows();
                log.info("seed.cleaned service=review version={} scope=synthetic_only completed=true",
                        VERSION);
            }

            repository.insertReviews(dataset.reviews());
            repository.insertVotes(dataset.votes());
            repository.insertRatings(dataset.ratings());
            repository.insertEvents(dataset.events());
            validator.validate(dataset, VERSION);

            long rows = (long) dataset.reviews().size() + dataset.votes().size()
                    + dataset.ratings().size() + dataset.events().size();
            repository.writeMarker(VERSION, rows,
                    "seeder review-service: " + dataset.reviews().size() + " reviews, "
                            + dataset.votes().size() + " votes, "
                            + dataset.ratings().size() + " product ratings, "
                            + dataset.events().size() + " events");
            validator.validateMarker(VERSION);

            SeedExecution execution = SeedExecution.completed("review", VERSION,
                    dataset.reviews().size(), dataset.votes().size(),
                    dataset.ratings().size(), dataset.events().size(),
                    System.currentTimeMillis() - started);
            log.info("seed.completed service=review version={} reviews={} votes={} ratings={} "
                            + "outbox={} durationMs={} outcome={} completed=true",
                    VERSION, execution.reviews(), execution.votes(), execution.ratings(),
                    execution.outbox(), execution.durationMs(), execution.outcome());
            metrics.record(execution);
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service=review version={} error={} completed=false",
                    VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }
}
