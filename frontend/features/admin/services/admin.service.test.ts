import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { mockState } = vi.hoisted(() => ({
  mockState: { useMocks: false, token: "test-token" },
}));

vi.mock("next/cache", () => ({ cacheLife: vi.fn() }));
vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" && mockState.token ? { value: mockState.token } : undefined,
  }),
}));
vi.mock("@/config/env", () => ({
  env: {
    apiBaseUrl: "http://localhost:8081",
    get useMocks() {
      return mockState.useMocks;
    },
  },
}));

import { httpClient } from "@/lib/http/client";

import {
  ADMIN_PRODUCTS_DEFAULT_SORT,
  ADMIN_PRODUCTS_PAGE_SIZE,
  getAdminCategories,
  getAdminNavigation,
  getAdminProducts,
  getAdminSummary,
} from "./admin.service";

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

describe("admin.service", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    mockState.useMocks = false;
    mockState.token = "test-token";
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
    expect(result.items[0]).toMatchObject({
      id: "7",
      name: "Hogar",
      slug: "hogar",
      featured: true,
      productCount: 18,
    });
    expect(result.selectedId).toBe("7");
  });

  it("keeps the wireframe seed in mock mode for categories", async () => {
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

  it("composes admin summary from real backend endpoints", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/orders/admin/summary") {
        return response({
          salesToday: 1000,
          salesYesterday: 500,
          ordersToday: 2,
          pendingOrders: 3,
          pendingWithoutPayment: 1,
          weekOrders: 10,
          previousWeekSales: 2000,
          salesByDay: [
            { date: "2026-11-04", total: 100, orders: 1, averageTicket: 100 },
            { date: "2026-11-05", total: 200, orders: 2, averageTicket: 100 },
            { date: "2026-11-06", total: 300, orders: 1, averageTicket: 300 },
            { date: "2026-11-07", total: 400, orders: 2, averageTicket: 200 },
            { date: "2026-11-08", total: 500, orders: 1, averageTicket: 500 },
            { date: "2026-11-09", total: 600, orders: 1, averageTicket: 600 },
            { date: "2026-11-10", total: 1000, orders: 2, averageTicket: 500 },
          ],
          recentOrders: [
            {
              id: 1042,
              orderNumber: "ORD-1042",
              customerId: "user-1",
              status: "PENDING",
              total: 24900,
              itemCount: 2,
              createdAt: "2026-11-10T12:42:00Z",
            },
          ],
        }) as never;
      }
      if (url === "/api/payments/admin/summary") {
        return response({ paymentsToClear: 86400, paymentsInReview: 3 }) as never;
      }
      if (url === "/api/products/admin/low-stock") {
        return response({
          items: [{ name: "Cinturón cuero café", sku: "CIN-CUE-CAF", quantity: 2 }],
          totalLowStock: 5,
          threshold: 5,
        }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminSummary();

    expect(get).toHaveBeenCalledWith("/api/orders/admin/summary", {
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/payments/admin/summary", {
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/products/admin/low-stock", {
      params: { threshold: 5, size: 5 },
      headers: { Authorization: "Bearer test-token" },
    });

    expect(result.salesToday).toBe(1000);
    expect(result.salesDelta).toBe(100);
    expect(result.paymentsToClear).toBe(86400);
    expect(result.paymentsInReview).toBe(3);
    expect(result.lowStockProducts[0]).toMatchObject({ sku: "CIN-CUE-CAF", quantity: 2 });
    expect(result.recentOrders[0]).toMatchObject({ id: "#1042", status: "pendiente" });
    expect(result.weekOrders).toBe(10);
    expect(result.recentActivity.length).toBeGreaterThan(0);
  });

  it("keeps mock summary in mock mode", async () => {
    mockState.useMocks = true;
    const seed = {
      dateLabel: "Hoy",
      updatedLabel: "Ahora",
      salesToday: 0,
      ordersToday: 0,
      salesDelta: 0,
      pendingOrders: 0,
      pendingWithoutPayment: 0,
      paymentsToClear: 0,
      paymentsInReview: 0,
      lowStockThreshold: 5,
      lowStockTotal: 0,
      weekDelta: 0,
      weekOrders: 0,
      salesByDay: [],
      recentOrders: [],
      lowStockProducts: [],
      recentActivity: [],
    };
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () => response(seed) as never);

    const result = await getAdminSummary();

    expect(get).toHaveBeenCalledWith("/admin/summary");
    expect(result).toEqual(seed);
  });

  it("composes admin navigation from real backend endpoints", async () => {
    vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/orders/admin/summary") {
        return response({ pendingOrders: 12 }) as never;
      }
      if (url === "/api/payments/admin/summary") {
        return response({ paymentsInReview: 3 }) as never;
      }
      if (url === "/api/products/admin/low-stock") {
        return response({ totalLowStock: 5, items: [], threshold: 5 }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminNavigation();

    expect(result).toEqual({ pendingOrders: 12, payments: 3, alerts: 5 });
  });

  it("reads the real admin products page with filters, bearer auth and DTO mapping", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/products/admin") {
        return response({
          content: [
            {
              productId: "42",
              name: "Auriculares inalámbricos",
              sku: "AUR-INL-NEG",
              category: "Tecnología",
              price: "49999.00",
              stock: 7,
              status: "publicado",
              published: true,
            },
          ],
          totalElements: 12,
          totalPages: 2,
          size: 8,
          number: 1,
        }) as never;
      }
      if (url === "/api/products/admin/summary") {
        return response({
          totalProducts: 48,
          totalCategories: 6,
          totalLowStock: 5,
          lowStockThreshold: 5,
        }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminProducts({
      q: "auriculares",
      status: "publicado",
      categoryId: "7",
      page: 2,
      size: 8,
      sort: "title,asc",
    });

    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: {
        q: "auriculares",
        status: "publicado",
        categoryId: "7",
        page: 1,
        size: 8,
        sort: "title,asc",
      },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/products/admin/summary", {
      params: { q: "auriculares", categoryId: "7" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(result.items).toEqual([
      {
        id: "42",
        productId: "42",
        name: "Auriculares inalámbricos",
        sku: "AUR-INL-NEG",
        category: "Tecnología",
        price: 49999,
        stock: 7,
        status: "publicado",
        published: true,
      },
    ]);
    expect(result).toMatchObject({
      pageSize: 8,
      totalProducts: 48,
      totalCategories: 6,
      totalLowStock: 5,
      lowStockThreshold: 5,
    });
  });

  it("uses the wireframe page size and a recent-first sort by default", async () => {
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

    const result = await getAdminProducts();

    expect(ADMIN_PRODUCTS_PAGE_SIZE).toBe(8);
    expect(ADMIN_PRODUCTS_DEFAULT_SORT).toBe("id,desc");
    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: { page: 0, size: 8, sort: "id,desc" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(result.items).toEqual([]);
  });

  it("ignores mock mode and always reads the real admin endpoints", async () => {
    mockState.useMocks = true;
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

    const result = await getAdminProducts();

    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: { page: 0, size: 8, sort: "id,desc" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/products/admin/summary", {
      params: {},
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).not.toHaveBeenCalledWith("/admin/products");
    expect(result.items).toEqual([]);
  });
});
