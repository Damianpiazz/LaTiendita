import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { mockState } = vi.hoisted(() => ({
  mockState: { token: "test-token", useMocks: true },
}));

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
  ADMIN_CLIENTS_DEFAULT_SORT,
  ADMIN_CLIENTS_PAGE_SIZE,
  getAdminClientDetail,
  getAdminClients,
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

const customerDto = {
  customerId: "c-1",
  firstName: "Ana",
  lastName: "Torres",
  email: "ana.torres@mail.com",
  phone: "+54 11 5555-1038",
  status: "ACTIVE",
  createdAt: "2026-10-20T10:00:00Z",
  lastLoginAt: "2026-11-10T09:00:00Z",
  orderCount: 3,
  totalSpent: 150000,
  lastPurchaseAt: "2026-11-10T09:42:00Z",
  segment: "nueva",
};

const summaryDto = {
  totalCustomers: 128,
  newThisMonth: 24,
  recurring: 41,
  averageTicket: 27430,
};

describe("getAdminClients real adapter", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    mockState.token = "test-token";
    mockState.useMocks = true;
  });

  it("reads the Spring page with Bearer, forwards filters and derives the KPIs from the summary", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/customers/admin") {
        return response({ content: [customerDto], totalElements: 128 }) as never;
      }
      if (url === "/api/customers/admin/summary") {
        return response(summaryDto) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminClients({
      page: 2,
      size: 8,
      name: "Ana",
      email: "ana@",
      phone: "5555",
      status: "ACTIVE",
      segment: "nueva",
      sort: "lastName,asc",
    });

    expect(get).toHaveBeenCalledWith("/api/customers/admin", {
      params: {
        page: 1,
        size: 8,
        sort: "lastName,asc",
        name: "Ana",
        email: "ana@",
        phone: "5555",
        status: "ACTIVE",
        segment: "nueva",
      },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/customers/admin/summary", {
      params: {
        name: "Ana",
        email: "ana@",
        phone: "5555",
        status: "ACTIVE",
        segment: "nueva",
      },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(result).toEqual({
      items: [
        {
          customerId: "c-1",
          firstName: "Ana",
          lastName: "Torres",
          name: "Ana Torres",
          initials: "AT",
          email: "ana.torres@mail.com",
          phone: "+54 11 5555-1038",
          orderCount: 3,
          totalSpent: 150000,
          lastPurchase: "10/11 · 09:42",
          lastPurchaseAt: "2026-11-10T09:42:00Z",
          createdAt: "2026-10-20T10:00:00Z",
          lastLoginAt: "2026-11-10T09:00:00Z",
          segment: "nueva",
          status: "nueva",
        },
      ],
      pageSize: 8,
      totalClients: 128,
      newThisMonth: 24,
      recurring: 41,
      averageTicket: 27430,
    });
  });

  it("never reads the mock route and keeps the wireframe page defaults", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/customers/admin") {
        return response({ content: [], totalElements: 0 }) as never;
      }
      if (url === "/api/customers/admin/summary") {
        return response({ ...summaryDto, totalCustomers: 0 }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminClients();

    expect(ADMIN_CLIENTS_PAGE_SIZE).toBe(8);
    expect(ADMIN_CLIENTS_DEFAULT_SORT).toBe("lastPurchaseAt,desc");
    expect(get).toHaveBeenCalledWith("/api/customers/admin", {
      params: { page: 0, size: 8, sort: "lastPurchaseAt,desc" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).not.toHaveBeenCalledWith("/admin/clients");
    expect(result.items).toEqual([]);
    expect(result.totalClients).toBe(0);
  });

  it("reads the detail from the reused customer endpoint with Bearer and the list stats", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        id: "c-1",
        firstName: "Ana",
        lastName: "Torres",
        email: "ana.torres@mail.com",
        phone: "+54 11 5555-1038",
        status: "ACTIVE",
        createdAt: "2026-10-20T10:00:00Z",
        lastLoginAt: "2026-11-10T09:00:00Z",
      }) as never,
    );
    const row = {
      customerId: "c-1",
      firstName: "Ana",
      lastName: "Torres",
      name: "Ana Torres",
      initials: "AT",
      email: "ana.torres@mail.com",
      phone: "+54 11 5555-1038",
      orderCount: 3,
      totalSpent: 150000,
      lastPurchase: "10/11 · 09:42",
      lastPurchaseAt: "2026-11-10T09:42:00Z",
      createdAt: "2026-10-20T10:00:00Z",
      lastLoginAt: "2026-11-10T09:00:00Z",
      segment: "nueva" as const,
      status: "nueva" as const,
    };

    const detail = await getAdminClientDetail("c-1", row);

    expect(get).toHaveBeenCalledWith("/api/customers/c-1", {
      headers: { Authorization: "Bearer test-token" },
    });
    expect(detail).toEqual({
      ...row,
      createdAt: "2026-10-20T10:00:00Z",
      lastLoginAt: "2026-11-10T09:00:00Z",
    });
  });

  it("keeps sin-compras distinct from inactiva and forwards phone to list and summary", async () => {
    const sinComprasDto = {
      ...customerDto,
      customerId: "c-2",
      orderCount: 0,
      lastPurchaseAt: null,
      segment: "sin-compras",
    };
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/customers/admin") {
        return response({ content: [sinComprasDto], totalElements: 1 }) as never;
      }
      if (url === "/api/customers/admin/summary") {
        return response({ ...summaryDto, totalCustomers: 1 }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await getAdminClients({
      phone: "5555",
      segment: "sin-compras",
    });

    expect(get).toHaveBeenCalledWith("/api/customers/admin", {
      params: {
        page: 0,
        size: 8,
        sort: "lastPurchaseAt,desc",
        phone: "5555",
        segment: "sin-compras",
      },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/customers/admin/summary", {
      params: { phone: "5555", segment: "sin-compras" },
      headers: { Authorization: "Bearer test-token" },
    });
    expect(result.items[0]).toMatchObject({
      customerId: "c-2",
      segment: "sin-compras",
      status: "sin-compras",
    });
  });
});
