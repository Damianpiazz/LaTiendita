import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { mockState, service } = vi.hoisted(() => ({
  mockState: { token: "test-token" },
  service: { getAdminClients: vi.fn(), getAdminClientDetail: vi.fn() },
}));

vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" && mockState.token ? { value: mockState.token } : undefined,
  }),
}));
vi.mock("next/cache", () => ({ revalidatePath: vi.fn() }));
vi.mock("@/features/admin/services/admin.service", () => service);

import { httpClient } from "@/lib/http/client";

import {
  exportAdminClients,
  getAdminClientDetail,
  listAdminClients,
} from "./client-actions";

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

describe("client administration actions", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.clearAllMocks();
    mockState.token = "test-token";
  });

  it("exports the filtered CSV through the gateway with the admin Bearer token", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response("customerId,name\n1,Ana Torres\n") as never,
    );

    const result = await exportAdminClients({
      name: "Ana",
      email: "ana@",
      phone: "5555",
      status: "ACTIVE",
      segment: "nueva",
      sort: "lastPurchaseAt,desc",
    });

    expect(get).toHaveBeenCalledWith("/api/customers/admin/export.csv", {
      params: {
        name: "Ana",
        email: "ana@",
        phone: "5555",
        status: "ACTIVE",
        segment: "nueva",
        sort: "lastPurchaseAt,desc",
      },
      headers: {
        Authorization: "Bearer test-token",
        Accept: "text/csv",
      },
      responseType: "text",
    });
    expect(result).toEqual({
      filename: "clientes.csv",
      content: "customerId,name\n1,Ana Torres\n",
    });
  });

  it("exports the sin-compras segment and phone without renaming the filter", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response("customerId,name\n2,Beto\n") as never,
    );

    await exportAdminClients({ phone: "5555", segment: "sin-compras" });

    expect(get).toHaveBeenCalledWith("/api/customers/admin/export.csv", {
      params: { phone: "5555", segment: "sin-compras" },
      headers: {
        Authorization: "Bearer test-token",
        Accept: "text/csv",
      },
      responseType: "text",
    });
  });

  it("forwards the whole list query to the real adapter", async () => {
    const page = { items: [], pageSize: 8, totalClients: 0, newThisMonth: 0, recurring: 0, averageTicket: 0 };
    service.getAdminClients.mockResolvedValueOnce(page);
    const query = { page: 2, size: 8, segment: "recurrente" as const };

    const result = await listAdminClients(query);

    expect(service.getAdminClients).toHaveBeenCalledWith(query);
    expect(result).toBe(page);
  });

  it("reads the customer detail on the server without handing a token to the caller", async () => {
    const detail = { customerId: "c-1", name: "Ana Torres" };
    service.getAdminClientDetail.mockResolvedValueOnce(detail);

    const result = await getAdminClientDetail("c-1");

    expect(service.getAdminClientDetail).toHaveBeenCalledWith("c-1", undefined);
    expect(result).toBe(detail);
    expect(JSON.stringify(result)).not.toContain("test-token");
  });
});
