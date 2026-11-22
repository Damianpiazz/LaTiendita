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
  ADMIN_ORDERS_DEFAULT_SORT,
  ADMIN_ORDERS_PAGE_SIZE,
  ADMIN_PAYMENTS_DEFAULT_SORT,
  ADMIN_PAYMENTS_PAGE_SIZE,
  ADMIN_PAYMENTS_RANGE_DAYS,
  ADMIN_PRODUCTS_DEFAULT_SORT,
  ADMIN_PRODUCTS_PAGE_SIZE,
  getAdminCategories,
  getAdminNavigation,
  getAdminOrders,
  getAdminPayments,
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

  it("sends the bearer token and maps a Spring page of categories", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        content: [
          {
            categoryId: "7",
            name: "Hogar",
            description: "Artículos del hogar",
            slug: "hogar",
            featured: true,
            active: false,
            imageUrl: "/images/hogar.jpg",
            productCount: 18,
            lowStockCount: 4,
          },
        ],
        totalElements: 9,
      }) as never,
    );

    const result = await getAdminCategories();

    expect(get).toHaveBeenCalledWith("/api/categories", {
      params: { page: 0, sort: "name,asc" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(result.items).toEqual([
      {
        id: "7",
        name: "Hogar",
        slug: "hogar",
        description: "Artículos del hogar",
        productCount: 18,
        lowStockCount: 4,
        featured: true,
        active: false,
        imageUrl: "/images/hogar.jpg",
      },
    ]);
    expect(result.selectedId).toBe("7");
    expect(result.total).toBe(9);
  });

  it("forwards q, featured, sort and pagination with a 0-based page index", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({ content: [], totalElements: 0 }) as never,
    );

    await getAdminCategories({
      q: "remer",
      featured: true,
      page: 3,
      size: 6,
      sort: "name,desc",
    });

    expect(get).toHaveBeenCalledWith("/api/categories", {
      params: {
        q: "remer",
        featured: true,
        page: 2,
        size: 6,
        sort: "name,desc",
      },
      headers: { Authorization: "Bearer test-token" },
    });
  });

  it("never falls back to the mock categories route", async () => {
    mockState.useMocks = true;
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({ content: [], totalElements: 0 }) as never,
    );

    await getAdminCategories();

    expect(get).toHaveBeenCalledWith("/api/categories", expect.anything());
    expect(get).not.toHaveBeenCalledWith("/admin/categories");
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

  it("reads real admin orders with stats, auth and 0-based page", async () => {
    mockState.useMocks = true;
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/orders/admin") {
        return response({
          content: [
            {
              id: 1042,
              orderNumber: "ORD-1042",
              customerId: "customer-9",
              customerEmail: "lucia@mail.com",
              paymentMethod: null,
              status: "PENDING",
              total: "45998.00",
              itemCount: 3,
              createdAt: "2026-11-10T12:42:00Z",
            },
          ],
        }) as never;
      }
      if (url === "/api/orders/admin/stats") {
        return response({
          totalOrders: 48,
          pendingDispatch: 12,
          cancelledInPeriod: 2,
          statusCounts: {
            todos: 48,
            pendiente: 12,
            pagado: 8,
            enviado: 5,
            entregado: 21,
            cancelado: 2,
          },
        }) as never;
      }
      if (url === "/api/payments/order/1042") {
        return response({ provider: "MERCADO_PAGO" }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminOrders({ status: "pendiente", page: 2, q: "1042" });

    expect(ADMIN_ORDERS_PAGE_SIZE).toBe(8);
    expect(ADMIN_ORDERS_DEFAULT_SORT).toBe("createdAt,desc");
    expect(get).toHaveBeenCalledWith("/api/orders/admin", {
      params: { page: 1, size: 8, sort: "createdAt,desc", q: "1042", status: "PENDING" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/orders/admin/stats", {
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).not.toHaveBeenCalledWith("/admin/orders");
    expect(result.totalOrders).toBe(48);
    expect(result.statusCounts.pendiente).toBe(12);
    expect(result.items[0]).toMatchObject({
      id: "#1042",
      customer: "customer-9",
      email: "lucia@mail.com",
      payment: "mercado-pago",
      status: "pendiente",
      itemCount: 3,
    });
  });

  it("reads the real payments page with bearer auth, a 0-based page and mapped rows", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/payments/admin") {
        return response({
          content: [
            {
              id: 812,
              orderNumber: "ORD-1042",
              customerId: "user-1",
              provider: "MERCADO_PAGO",
              paymentMethodId: "visa",
              paymentType: "credit_card",
              paidAt: "2026-11-10T12:42:00Z",
              amount: "45998.00",
              status: "PENDING",
            },
          ],
          totalElements: 48,
          totalPages: 6,
          size: 8,
          number: 1,
        }) as never;
      }
      if (url === "/api/payments/admin/summary") {
        return response({
          paymentsToClear: 86400,
          paymentsInReview: 11,
          totalPayments: 48,
          approvedCount: 34,
          pendingCount: 11,
          rejectedCount: 3,
          approvedVolume: 1284950,
          pendingVolume: 412370,
          rejectedVolume: 67994,
          approvalRate: 91,
          approvalAttempts: 37,
          rangeDays: 30,
        }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminPayments({
      q: "1042",
      status: "pendiente",
      page: 2,
      size: 8,
      sort: "amount,desc",
    });

    expect(get).toHaveBeenCalledWith("/api/payments/admin", {
      params: {
        q: "1042",
        status: "PENDING",
        page: 1,
        size: 8,
        sort: "amount,desc",
        days: 30,
      },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/payments/admin/summary", {
      params: { days: 30 },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).not.toHaveBeenCalledWith("/admin/payments");
    expect(result.items).toEqual([
      {
        id: "#812",
        orderId: "#1042",
        customer: "user-1",
        provider: "Mercado Pago",
        method: "Tarjeta de crédito · Visa",
        paidAt: "10/11 · 09:42",
        amount: 45998,
        status: "pendiente",
      },
    ]);
    expect(result).toMatchObject({
      pageSize: 8,
      totalPayments: 48,
      approvedCount: 34,
      pendingCount: 11,
      rejectedCount: 3,
      approvalRate: 91,
      approvalAttempts: 37,
      rangeLabel: "Últimos 30 días",
    });
  });

  it("uses the wireframe page size, sort and 30-day range by default even in mock mode", async () => {
    mockState.useMocks = true;
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/payments/admin") {
        return response({ content: [] }) as never;
      }
      if (url === "/api/payments/admin/summary") {
        return response({
          paymentsToClear: 0,
          paymentsInReview: 0,
          totalPayments: 0,
          approvedCount: 0,
          pendingCount: 0,
          rejectedCount: 0,
          approvedVolume: 0,
          pendingVolume: 0,
          rejectedVolume: 0,
          approvalRate: 0,
          approvalAttempts: 0,
          rangeDays: 30,
        }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminPayments();

    expect(ADMIN_PAYMENTS_PAGE_SIZE).toBe(8);
    expect(ADMIN_PAYMENTS_DEFAULT_SORT).toBe("paidAt,desc");
    expect(ADMIN_PAYMENTS_RANGE_DAYS).toBe(30);
    expect(get).toHaveBeenCalledWith("/api/payments/admin", {
      params: { page: 0, size: 8, sort: "paidAt,desc", days: 30 },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).not.toHaveBeenCalledWith("/admin/payments");
    expect(result.items).toEqual([]);
  });

  it("preserves the admin products bearer and 0-based page after adding the payments adapter", async () => {
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

    const result = await getAdminProducts({ page: 3, status: "publicado" });

    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: { page: 2, size: 8, sort: "id,desc", status: "publicado" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(result.items).toEqual([]);
  });

  it("regression: clients work keeps the products bearer and 0-based page", async () => {
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

    await getAdminProducts({ page: 4, size: 8 });

    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: { page: 3, size: 8, sort: "id,desc" },
      headers: { Authorization: "Bearer test-token" },
    });
  });

  it("regression: clients work keeps the categories bearer and 0-based page", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({ content: [], totalElements: 0 }) as never,
    );

    await getAdminCategories({ page: 3, q: "remer" });

    expect(get).toHaveBeenCalledWith("/api/categories", {
      params: { page: 2, sort: "name,asc", q: "remer" },
      headers: { Authorization: "Bearer test-token" },
    });
  });
});
