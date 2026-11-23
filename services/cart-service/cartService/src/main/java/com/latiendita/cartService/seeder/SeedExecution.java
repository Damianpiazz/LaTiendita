package com.latiendita.cartService.seeder;

/** Resultado de una ejecución del seeder, usado por logs, métricas y marcador. */
public record SeedExecution(
        String service,
        String version,
        boolean executed,
        int carts,
        int items,
        long durationMs,
        String outcome
) {

    public static SeedExecution skipped(String service, String version) {
        return new SeedExecution(service, version, false, 0, 0, 0L, "already_seeded");
    }

    public static SeedExecution completed(String service, String version,
                                          int carts, int items, long durationMs) {
        return new SeedExecution(service, version, true, carts, items, durationMs,
                "inserted_and_verified");
    }
}
