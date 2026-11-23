import type { AxiosResponse } from "axios";
import { describe, it, vi } from "vitest";
vi.mock("next/headers", () => ({ cookies: async () => ({ get: () => ({ value: "parity-token" }) }) }));
vi.mock("@/config/env", () => ({ env: { apiBaseUrl: "http://gateway", useMocks: false } }));
import { httpClient } from "@/lib/http/client";
import { getAdminCategories, getAdminProducts } from "@/features/admin/services/admin.service";

function res<T>(data: T): AxiosResponse<T> {
  return { data, status: 200, statusText: "OK", headers: {}, config: { headers: {} }, request: {} } as AxiosResponse<T>;
}

describe("T3 baseline parity for pre-existing commands", () => {
  it("prints exact adapter calls and outputs", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/products/admin") return res({ content: [{ id: 1, name: "A", price: 10, categoryId: 7, slug: "a" }], totalElements: 40 }) as never;
      if (url === "/api/products/admin/summary") return res({ totalProducts: 40, totalCategories: 6, lowStockThreshold: 5, totalLowStock: 2 }) as never;
      if (url === "/api/categories") return res({ content: [{ categoryId: 7, name: "Hogar", slug: "hogar", productCount: 3 }], totalElements: 6 }) as never;
      throw new Error(url);
    });
    const products = await getAdminProducts({ page: 3, size: 5, q: "x", status: "publicado", categoryId: "7", sort: "name,asc" });
    const categories = await getAdminCategories({ page: 3, q: "x", featured: false, size: 5, sort: "name,asc" });
    console.log("T3_PARITY_CALLS=" + JSON.stringify(get.mock.calls));
    console.log("T3_PARITY_RESULT=" + JSON.stringify({ products, categories }));
  });
});
