import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const probe = vi.hoisted(() => ({ token: "probe-token" as string | null }));
vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" && probe.token ? { value: probe.token } : undefined,
  }),
}));
vi.mock("@/config/env", () => ({
  env: { apiBaseUrl: "http://gateway", useMocks: true },
}));

import { httpClient } from "@/lib/http/client";
import { exportAdminClients } from "@/features/admin/actions/client-actions";
import { getAdminClientDetail, getAdminClients } from "./admin.service";

function res<T>(data: T): AxiosResponse<T> {
  return { data, status: 200, statusText: "OK", headers: {}, config: { headers: {} }, request: {} } as AxiosResponse<T>;
}

const dto = {
  customerId: "c-9",
  firstName: "Lucia",
  lastName: "Gomez",
  email: "lucia@mail.com",
  phone: "555-9999",
  status: "SUSPENDED",
  createdAt: "2026-11-02T10:00:00Z",
  lastLoginAt: "2026-11-19T10:00:00Z",
  orderCount: 0,
  totalSpent: 0,
  lastPurchaseAt: null,
  segment: "sin-compras",
};

function stub() {
  return vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
    if (url === "/api/customers/admin") return res({ content: [dto], totalElements: 137 }) as never;
    if (url === "/api/customers/admin/summary") {
      return res({ totalCustomers: 137, newThisMonth: 9, recurring: 21, averageTicket: 4321 }) as never;
    }
    if (url.startsWith("/api/customers/")) return res({ id: "c-9", firstName: "Lucia" }) as never;
    throw new Error(`unexpected url ${url}`);
  });
}

beforeEach(() => {
  vi.restoreAllMocks();
  probe.token = "probe-token";
});

describe("T3 independent admin clients probes", () => {
  it("converts 1-based UI page to 0-based wire page and forwards every filter with Bearer", async () => {
    const get = stub();
    const out = await getAdminClients({
      page: 4, size: 8, name: "Lucia", email: "lucia@", phone: "555",
      status: "SUSPENDED", segment: "sin-compras", sort: "email,asc",
    });
    expect(get).toHaveBeenCalledWith("/api/customers/admin", {
      params: { page: 3, size: 8, sort: "email,asc", name: "Lucia", email: "lucia@",
        phone: "555", status: "SUSPENDED", segment: "sin-compras" },
      headers: { Authorization: "Bearer probe-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/customers/admin/summary", {
      params: { name: "Lucia", email: "lucia@", phone: "555", status: "SUSPENDED", segment: "sin-compras" },
      headers: { Authorization: "Bearer probe-token" },
    });
    expect(out).toMatchObject({
      pageSize: 8, totalClients: 137, newThisMonth: 9, recurring: 21, averageTicket: 4321,
    });
    expect(out.items[0]).toMatchObject({ customerId: "c-9", segment: "sin-compras", status: "sin-compras" });
  });

  it("defaults and clamped page stay 1-based in UI and 0-based on the wire", async () => {
    const get = stub();
    await getAdminClients({ page: 0 });
    expect(get.mock.calls.find((c) => c[0] === "/api/customers/admin")?.[1]?.params)
      .toMatchObject({ page: 0, size: 8, sort: "lastPurchaseAt,desc" });
    get.mockClear();
    await getAdminClients({ page: 1 });
    expect(get.mock.calls.find((c) => c[0] === "/api/customers/admin")?.[1]?.params)
      .toMatchObject({ page: 0 });
    expect(get.mock.calls.every((c) => c[0] !== "/admin/clients")).toBe(true);
  });

  it("export action forwards Bearer, filters and CSV response type", async () => {
    const get = vi.spyOn(httpClient, "get").mockResolvedValueOnce(res("customerId,segment\nc-9,sin-compras\n") as never);
    const out = await exportAdminClients({ name: "Lucia", phone: "555", segment: "sin-compras", sort: "email,asc" });
    expect(get).toHaveBeenCalledWith("/api/customers/admin/export.csv", {
      params: { name: "Lucia", phone: "555", segment: "sin-compras", sort: "email,asc" },
      headers: { Authorization: "Bearer probe-token", Accept: "text/csv" },
      responseType: "text",
    });
    expect(out.filename).toBe("clientes.csv");
    expect(out.content).toContain("sin-compras");
  });

  it("detail reuses the customer endpoint with Bearer and an encoded id", async () => {
    const get = vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({ id: "c-9", firstName: "Lucia" }) as never);
    const detail = await getAdminClientDetail("a/b", { ...dto, status: "sin-compras" } as never);
    expect(get).toHaveBeenCalledWith("/api/customers/a%2Fb", {
      headers: { Authorization: "Bearer probe-token" },
    });
    expect(detail.customerId).toBe("c-9");
  });

  it("empty string filters are omitted rather than sent", async () => {
    const get = stub();
    await getAdminClients({ name: "", email: "", phone: "", segment: undefined });
    expect(get.mock.calls[0][1]?.params).toEqual({ page: 0, size: 8, sort: "lastPurchaseAt,desc" });
  });
});
