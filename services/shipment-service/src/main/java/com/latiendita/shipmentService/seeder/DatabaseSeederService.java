package com.latiendita.shipmentService.seeder;

import com.latiendita.shipmentService.seeder.model.SeedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Orquesta la siembra de `shipments`, `delivery_addresses` y `outbox_events`. */
@Service
public class DatabaseSeederService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);
    static final String VERSION = "shipment-v1";

    private final SeederProperties properties;
    private final SeederProfileGuard guard;
    private final ShipmentDataGenerator generator;
    private final SeederJdbcRepository repository;
    private final SeederValidator validator;
    private final SeederMetrics metrics;

    public DatabaseSeederService(SeederProperties properties, SeederProfileGuard guard,
                                 ShipmentDataGenerator generator, SeederJdbcRepository repository,
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
                SeedExecution skipped = SeedExecution.skipped("shipment", VERSION);
                log.info("seed.skipped service=shipment version={} reason=already_seeded completed=true",
                        VERSION);
                metrics.record(skipped);
                return skipped;
            }

            if (properties.isCleanBeforeRun()) {
                repository.cleanSeededRows();
                log.info("seed.cleaned service=shipment version={} scope=synthetic_only completed=true",
                        VERSION);
            }

            SeedDataset dataset = generator.generate();
            repository.insertAddresses(dataset.addresses());
            repository.insertShipments(dataset.shipments());
            if (properties.isIncludeInfra()) {
                repository.replaceSeededEvents(properties.getExternalIdPrefix(), dataset.events());
            }
            validator.validate(dataset, VERSION);
            repository.writeMarker(VERSION, dataset.shipments().size() + dataset.addresses().size()
                    + dataset.events().size(), "seeder shipment-service: "
                    + dataset.shipments().size() + " shipments, " + dataset.addresses().size()
                    + " addresses, " + dataset.events().size() + " events");
            validator.validateMarker(VERSION);

            SeedExecution execution = SeedExecution.completed("shipment", VERSION,
                    dataset.shipments().size(), dataset.addresses().size(), dataset.events().size(),
                    System.currentTimeMillis() - started);
            log.info("seed.completed service=shipment version={} shipments={} addresses={} events={} "
                            + "durationMs={} outcome={} completed=true",
                    VERSION, execution.shipments(), execution.addresses(), execution.events(),
                    execution.durationMs(), execution.outcome());
            metrics.record(execution);
            return execution;
        } catch (RuntimeException ex) {
            metrics.recordFailure();
            log.error("seed.failed service=shipment version={} error={} completed=false",
                    VERSION, ex.getMessage(), ex);
            throw ex;
        }
    }
}
