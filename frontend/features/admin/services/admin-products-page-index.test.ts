import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { mockState } = vi.hoisted(() => ({
  mockState: { token: "test-token" },
}));

vi.mock("next/cache", () => ({ cacheLife: vi.fn() }));
vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" && mockState.token ? { value: mockState.token } : undefined,
  }),
}));
vi.mock("@/config/env", () => ({
  env: { apiBaseUrl: "http://localhost:8081", useMocks: false },
}));

import { httpClient } from "@/lib/http/client";

import { getAdminProducts } from "./admin.service";

function response<T>(data: T): AxiosResponse<T> {
  return {
    data,
    status: 200,
    statusText: "OK",
    headers: {},
    config: { headers: {} },
    request: {},
  } as AxiosResponse<T>;
}

describe("verifier page index contract", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    mockState.token = "test-token";
  });

  it("sends Spring Data page 0 for the first admin table page", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/products/admin") {
        return response({ content: [] }) as never;
      }
      if (url === "/api/products/admin/summary") {
        return response({
          totalProducts: 0,
          totalCategories: 0,
          totalLowStock: 0,
          lowStockThreshold: 5,
        }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    await getAdminProducts();

    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: { page: 0, size: 8, sort: "id,desc" },
      headers: { Authorization: "Bearer test-token" },
    });
  });
});
