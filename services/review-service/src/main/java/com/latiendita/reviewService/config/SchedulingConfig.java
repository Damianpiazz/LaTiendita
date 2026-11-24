package com.latiendita.reviewService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Kept separate from the application class so JPA/web slices start cleanly. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
