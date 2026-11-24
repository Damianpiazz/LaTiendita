package com.latiendita.reviewService.seeder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Doble guarda de seguridad: el seeder sólo corre en perfiles explícitamente
 * permitidos y nunca en producción, aunque alguien lo habilite por variable de
 * entorno. La limpieza exige además un segundo interruptor (`allowClean`).
 */
@Component
public class SeederProfileGuard {

    private static final Set<String> PRODUCTION_PROFILES = Set.of("prod", "production", "prd");

    private final SeederProperties properties;
    private final Environment environment;

    public SeederProfileGuard(SeederProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    public void validate() {
        List<String> profiles = activeProfiles();

        boolean production = profiles.stream()
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(PRODUCTION_PROFILES::contains);
        if (production) {
            throw new IllegalStateException(
                    "El seeder está prohibido en perfiles de producción: " + profiles);
        }

        List<String> allowed = properties.getAllowedProfiles().stream()
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .toList();
        boolean permitted = profiles.stream()
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(allowed::contains);
        if (!permitted) {
            throw new IllegalStateException(
                    "El seeder requiere uno de estos perfiles: " + properties.getAllowedProfiles()
                            + ". Perfiles activos: " + profiles
                            + ". Ejemplo: SPRING_PROFILES_ACTIVE=local");
        }
    }

    public void validateClean() {
        validate();
        if (properties.isCleanBeforeRun() && !properties.isAllowClean()) {
            throw new IllegalStateException(
                    "SEEDER_CLEAN_BEFORE_RUN=true requiere SEEDER_ALLOW_CLEAN=true");
        }
    }

    private List<String> activeProfiles() {
        String[] active = environment.getActiveProfiles();
        if (active.length == 0) {
            active = environment.getDefaultProfiles();
        }
        return Arrays.stream(active).toList();
    }
}
