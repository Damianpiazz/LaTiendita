package com.latiendita.customerService.seeder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SeederProfileGuardTest {

    @Test
    void shouldAllowExplicitLocalProfile() {
        SeederProperties properties = properties();
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");

        assertThatCode(() -> new SeederProfileGuard(properties, environment).validate())
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectProductionEvenIfExplicitlyAllowed() {
        SeederProperties properties = properties();
        properties.setAllowedProfiles(List.of("prod", "local"));
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> new SeederProfileGuard(properties, environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("producción");
    }

    @Test
    void shouldRejectDefaultProfileWhenNotAllowed() {
        SeederProperties properties = properties();
        MockEnvironment environment = new MockEnvironment();

        assertThatThrownBy(() -> new SeederProfileGuard(properties, environment).validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SPRING_PROFILES_ACTIVE=local");
    }

    @Test
    void shouldRequireSecondSwitchForCleaning() {
        SeederProperties properties = properties();
        properties.setCleanBeforeRun(true);
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");

        assertThatThrownBy(() -> new SeederProfileGuard(properties, environment).validateClean())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SEEDER_ALLOW_CLEAN=true");

        properties.setAllowClean(true);
        assertThatCode(() -> new SeederProfileGuard(properties, environment).validateClean())
                .doesNotThrowAnyException();
    }

    private static SeederProperties properties() {
        SeederProperties properties = new SeederProperties();
        properties.setAllowedProfiles(List.of("dev", "test", "local"));
        return properties;
    }
}
