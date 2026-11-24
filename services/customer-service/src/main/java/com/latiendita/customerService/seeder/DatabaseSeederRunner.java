package com.latiendita.customerService.seeder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Dispara el seeder al arrancar, sólo cuando `SEEDER_ENABLED=true`. */
@Component
@ConditionalOnProperty(prefix = "seeder", name = "enabled", havingValue = "true")
public class DatabaseSeederRunner implements ApplicationRunner {

    private final DatabaseSeederService service;

    public DatabaseSeederRunner(DatabaseSeederService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        service.run();
    }
}
