package com.latiendita.productService.seeder;

/** Resultado de una ejecución del seeder, usado por logs, métricas y marcador. */
public record SeedExecution(
        String service,
        String version,
        boolean executed,
        int categories,
        int products,
        long durationMs,
        String outcome
) {

    public static SeedExecution skipped(String service, String version) {
        return new SeedExecution(service, version, false, 0, 0, 0L, "already_seeded");
    }

    public static SeedExecution completed(String service, String version,
                                          int categories, int products, long durationMs) {
        return new SeedExecution(service, version, true, categories, products, durationMs,
                "inserted_and_verified");
    }
}
