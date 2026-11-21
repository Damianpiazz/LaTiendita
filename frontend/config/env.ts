const DEFAULT_API_BASE_URL = "http://localhost:8081";

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
   * Real backend by default. Mock mode is an explicit opt-in for isolated
   * tests; an absent or empty flag never enables it.
   */
  useMocks: readBoolean(process.env.NEXT_PUBLIC_USE_MOCKS, false),
} as const;
