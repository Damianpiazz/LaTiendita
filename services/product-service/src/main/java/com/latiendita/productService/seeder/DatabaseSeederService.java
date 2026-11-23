package com.latiendita.productService.seeder;

import com.latiendita.productService.seeder.model.SeedDataset;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Orquesta el seeder: guardas → generación → inserción → verificación → marcador. */
@Service
public class DatabaseSeederService {

    static final String VERSION = "product-v1";

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final ProductDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;
    private final ObjectMapper mapper = new ObjectMapper();

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 ProductDataGenerator generator, SeederJdbcRepository repository,
                                 SeederValidator validator, SeederMetrics metrics) {
        this.properties = properties;
        this.guard = guard;
        this.generator = generator;
        this.repository = repository;
        this.validator = validator;
        this.metrics = metrics;
    }

    public SeedExecution run() {
        long started = System.currentTimeMillis();
        try {
            guard.validate();
            repository.ensureMarkerTable();

            if (repository.markerExists(VERSION)) {
                SeedExecution skipped = SeedExecution.skipped(
                        SeederJdbcRepository.SERVICE, VERSION);
                log.info("seed.skipped service={} version={} reason={}",
                        skipped.service(), skipped.version(), skipped.outcome());
                metrics.record(skipped);
                return skipped;
            }

            if (properties.isCleanBeforeRun()) {
                guard.validateClean();
                repository.cleanSeededRows();
                log.info("seed.cleaned service={} prefix={}",
                        SeederJdbcRepository.SERVICE, properties.getExternalIdPrefix());
            }

            SeedDataset dataset = generator.generate();
            repository.insertCategories(dataset.categories());
            repository.insertProducts(dataset.products());
            repository.resetIdentity("categories");
            repository.resetIdentity("products");

            validator.validate(dataset, VERSION);
            repository.writeMarker(VERSION,
                    dataset.categories().size() + (long) dataset.products().size(),
                    details(dataset));
            validator.validateMarker(VERSION);

            long duration = System.currentTimeMillis() - started;
            SeedExecution execution = SeedExecution.completed(SeederJdbcRepository.SERVICE,
                    VERSION, dataset.categories().size(), dataset.products().size(), duration);
            metrics.record(execution);
            log.info("seed.completed service={} version={} categories={} products={} "
                            + "durationMs={} outcome={}",
                    execution.service(), execution.version(), execution.categories(),
                    execution.products(), execution.durationMs(), execution.outcome());
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service={} version={} error={}",
                    SeederJdbcRepository.SERVICE, VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }

    private String details(SeedDataset dataset) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("categories", dataset.categories().size());
        values.put("products", dataset.products().size());
        values.put("randomSeed", properties.getRandomSeed());
        values.put("batchSize", properties.getBatchSize());
        values.put("prefix", properties.getExternalIdPrefix());
        values.put("includeInfra", properties.isIncludeInfra());
        values.put("completedAt", Instant.now().toString());
        try {
            return mapper.writeValueAsString(values);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el marcador del seeder", ex);
        }
    }
}
