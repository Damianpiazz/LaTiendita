package com.latiendita.paymentService.seeder;

/** Resultado de una ejecución del seeder, usado por logs, métricas y marcador. */
public record SeedExecution(
        String service,
        String version,
        boolean executed,
        int payments,
        int events,
        int outbox,
        long durationMs,
        String outcome
) {

    public static SeedExecution skipped(String service, String version) {
        return new SeedExecution(service, version, false, 0, 0, 0, 0L, "already_seeded");
    }

    public static SeedExecution completed(String service, String version,
                                          int payments, int events, int outbox, long durationMs) {
        return new SeedExecution(service, version, true, payments, events, outbox, durationMs,
                "inserted_and_verified");
    }
}
