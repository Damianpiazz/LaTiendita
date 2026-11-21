import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("runtime environment", () => {
  beforeEach(() => {
    vi.resetModules();
  });

  afterEach(() => {
    vi.unstubAllEnvs();
  });

  it("uses the real backend when NEXT_PUBLIC_USE_MOCKS is absent", async () => {
    vi.stubEnv("NEXT_PUBLIC_USE_MOCKS", undefined);

    const { env } = await import("./env");

    expect(env.useMocks).toBe(false);
  });

  it("uses the real backend when NEXT_PUBLIC_USE_MOCKS is false", async () => {
    vi.stubEnv("NEXT_PUBLIC_USE_MOCKS", "false");

    const { env } = await import("./env");

    expect(env.useMocks).toBe(false);
  });

  it("keeps the gateway as the default API base URL", async () => {
    vi.stubEnv("NEXT_PUBLIC_API_URL", undefined);

    const { env } = await import("./env");

    expect(env.apiBaseUrl).toBe("http://localhost:8081");
  });
});
