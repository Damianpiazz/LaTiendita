const DEFAULT_API_BASE_URL = "http://localhost:8080";

function readBoolean(value: string | undefined, fallback: boolean): boolean {
  if (value === undefined || value.trim() === "") {
    return fallback;
  }

  return ["1", "true", "yes", "on"].includes(value.trim().toLowerCase());
}

/**
 * Public runtime configuration.
 *
 * `NEXT_PUBLIC_*` variables are read literally so Next.js can inline them in
 * client bundles.
 */
export const env = {
  apiBaseUrl: process.env.NEXT_PUBLIC_API_URL?.trim() || DEFAULT_API_BASE_URL,
  /**
   * Mock-first until the microservices are wired: every request goes through
   * `mocks/` unless the flag is explicitly disabled.
   */
  useMocks: readBoolean(process.env.NEXT_PUBLIC_USE_MOCKS, true),
} as const;
