package com.latiendita.paymentService.seeder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/** Ejecuta el seeder al arrancar, únicamente cuando `seeder.enabled=true`. */
@Component
@ConditionalOnProperty(prefix = "seeder", name = "enabled", havingValue = "true")
public class DatabaseSeederRunner implements ApplicationRunner, Ordered {

    private final DatabaseSeederService seeder;

    public DatabaseSeederRunner(DatabaseSeederService seeder) {
        this.seeder = seeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seeder.seed();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
