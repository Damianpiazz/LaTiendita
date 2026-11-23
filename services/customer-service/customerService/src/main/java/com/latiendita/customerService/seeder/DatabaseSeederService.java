package com.latiendita.customerService.seeder;

import com.latiendita.customerService.seeder.model.SeedDataset;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Orquesta la siembra determinista y sólo la marca como correcta tras validar PostgreSQL. */
@Service
public class DatabaseSeederService {

    static final String VERSION = "customer-v1";

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final CustomerDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;
    private final ObjectMapper mapper = new ObjectMapper();

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 CustomerDataGenerator generator, SeederJdbcRepository repository,
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
            repository.insertCustomers(dataset.customers());
            repository.replaceSeededEvents(properties.getExternalIdPrefix(), dataset.events());

            validator.validate(dataset, VERSION);
            repository.writeMarker(VERSION,
                    (long) dataset.customers().size() + dataset.events().size(),
                    details(dataset));
            validator.validateMarker(VERSION);

            long duration = System.currentTimeMillis() - started;
            SeedExecution execution = SeedExecution.completed(SeederJdbcRepository.SERVICE,
                    VERSION, dataset.customers().size(), dataset.events().size(), duration);
            metrics.record(execution);
            log.info("seed.completed service={} version={} customers={} events={} "
                            + "durationMs={} outcome={}",
                    execution.service(), execution.version(), execution.customers(),
                    execution.events(), execution.durationMs(), execution.outcome());
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
        values.put("customers", dataset.customers().size());
        values.put("events", dataset.events().size());
        values.put("randomSeed", properties.getRandomSeed());
        values.put("batchSize", properties.getBatchSize());
        values.put("prefix", properties.getExternalIdPrefix());
        values.put("externalIdBase", properties.getExternalIdBase());
        values.put("includeInfra", properties.isIncludeInfra());
        values.put("completedAt", Instant.now().toString());
        try {
            return mapper.writeValueAsString(values);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el marcador del seeder", ex);
        }
    }
}
