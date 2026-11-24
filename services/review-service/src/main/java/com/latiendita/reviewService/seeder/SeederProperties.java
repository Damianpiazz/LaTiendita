package com.latiendita.reviewService.seeder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Configuración del seeder, enlazada desde las variables `SEEDER_*`. */
@Component
@Validated
@ConfigurationProperties(prefix = "seeder")
public class SeederProperties {

    private boolean enabled;
    @Min(1) @Max(1_000_000)
    private int count = 1000;
    @Min(1) @Max(10_000)
    private int batchSize = 500;
    private long randomSeed = 12345L;
    private boolean cleanBeforeRun;
    private boolean allowClean;
    @NotEmpty
    private List<String> allowedProfiles = List.of("dev", "test", "local");
    private boolean includeInfra;
    @NotBlank
    private String externalIdPrefix = "SEED";
    @Min(0)
    private long externalIdBase = 9_000_000L;
    private boolean markerEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public boolean isCleanBeforeRun() {
        return cleanBeforeRun;
    }

    public void setCleanBeforeRun(boolean cleanBeforeRun) {
        this.cleanBeforeRun = cleanBeforeRun;
    }

    public boolean isAllowClean() {
        return allowClean;
    }

    public void setAllowClean(boolean allowClean) {
        this.allowClean = allowClean;
    }

    public List<String> getAllowedProfiles() {
        return allowedProfiles;
    }

    public void setAllowedProfiles(List<String> allowedProfiles) {
        this.allowedProfiles = allowedProfiles;
    }

    public boolean isIncludeInfra() {
        return includeInfra;
    }

    public void setIncludeInfra(boolean includeInfra) {
        this.includeInfra = includeInfra;
    }

    public String getExternalIdPrefix() {
        return externalIdPrefix;
    }

    public void setExternalIdPrefix(String externalIdPrefix) {
        this.externalIdPrefix = externalIdPrefix;
    }

    public long getExternalIdBase() {
        return externalIdBase;
    }

    public void setExternalIdBase(long externalIdBase) {
        this.externalIdBase = externalIdBase;
    }

    public boolean isMarkerEnabled() {
        return markerEnabled;
    }

    public void setMarkerEnabled(boolean markerEnabled) {
        this.markerEnabled = markerEnabled;
    }
}
