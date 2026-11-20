import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { mockState } = vi.hoisted(() => ({ mockState: { useMocks: false } }));

vi.mock("next/cache", () => ({ cacheLife: vi.fn() }));
vi.mock("@/config/env", () => ({
  env: {
    apiBaseUrl: "http://localhost:8081",
    get useMocks() {
      return mockState.useMocks;
    },
  },
}));

import { httpClient } from "@/lib/http/client";

import { getAdminCategories } from "./admin.service";

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

describe("getAdminCategories transport seam", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    mockState.useMocks = false;
  });

  it("reads the real catalog and maps it to the admin rows in real mode", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        content: [
          {
            categoryId: "7",
            name: "Hogar",
            description: "Artículos del hogar",
            slug: "hogar",
            featured: true,
            productCount: 18,
          },
        ],
      }) as never,
    );

    const result = await getAdminCategories();

    expect(get).toHaveBeenCalledWith("/api/categories");
    expect(result.items).toEqual([
      {
        id: "7",
        name: "Hogar",
        slug: "hogar",
        description: "Artículos del hogar",
        productCount: 18,
        lowStockCount: 0,
        featured: true,
      },
    ]);
    expect(result.selectedId).toBe("7");
  });

  it("keeps the wireframe seed in mock mode", async () => {
    mockState.useMocks = true;
    const seed = {
      items: [
        {
          id: "remeras",
          name: "Remeras",
          slug: "remeras",
          description: "Remeras, polos y remerones.",
          productCount: 14,
          lowStockCount: 3,
          featured: true,
        },
      ],
      selectedId: "remeras",
      updatedLabel: "hoy 09:12",
    };
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () => response(seed) as never);

    const result = await getAdminCategories();

    expect(get).toHaveBeenCalledWith("/admin/categories");
    expect(result).toEqual(seed);
  });
});
