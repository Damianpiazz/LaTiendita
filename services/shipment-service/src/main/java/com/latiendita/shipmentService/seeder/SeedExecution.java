package com.latiendita.shipmentService.seeder;

/** Resultado de una ejecución del seeder, usado por logs, métricas y marcador. */
public record SeedExecution(
        String service,
        String version,
        boolean executed,
        int shipments,
        int addresses,
        int events,
        long durationMs,
        String outcome
) {

    public static SeedExecution skipped(String service, String version) {
        return new SeedExecution(service, version, false, 0, 0, 0, 0L, "already_seeded");
    }

    public static SeedExecution completed(String service, String version,
                                          int shipments, int addresses, int events,
                                          long durationMs) {
        return new SeedExecution(service, version, true, shipments, addresses, events,
                durationMs, "inserted_and_verified");
    }
}
