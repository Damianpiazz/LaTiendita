import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { revalidatePath } = vi.hoisted(() => ({ revalidatePath: vi.fn() }));

vi.mock("next/cache", () => ({ cacheLife: vi.fn(), revalidatePath }));
vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" ? { value: "admin-token" } : undefined,
  }),
}));

import { httpClient } from "@/lib/http/client";

import { listAdminPayments } from "./payment-actions";

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

function summary() {
  return {
    paymentsToClear: 86400,
    paymentsInReview: 11,
    totalPayments: 20,
    approvedCount: 14,
    pendingCount: 4,
    rejectedCount: 2,
    approvedVolume: 120000,
    pendingVolume: 30000,
    rejectedVolume: 5000,
    approvalRate: 78,
    approvalAttempts: 18,
    rangeDays: 30,
  };
}

describe("admin payment actions", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    revalidatePath.mockClear();
  });

  it("refetches the real list with the bearer token and revalidates the payments path", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/payments/admin") {
        return response({ content: [], totalElements: 20 }) as never;
      }
      if (url === "/api/payments/admin/summary") {
        return response(summary()) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await listAdminPayments({
      q: "pago",
      status: "rechazado",
      page: 3,
      size: 8,
      sort: "amount,asc",
    });

    expect(get).toHaveBeenCalledWith("/api/payments/admin", {
      params: {
        q: "pago",
        status: "REJECTED",
        page: 2,
        size: 8,
        sort: "amount,asc",
        days: 30,
      },
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(get).toHaveBeenCalledWith("/api/payments/admin/summary", {
      params: { days: 30 },
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(revalidatePath).toHaveBeenCalledWith("/admin/pagos");
    expect(result).toMatchObject({ pageSize: 8, totalPayments: 20, rangeLabel: "Últimos 30 días" });
  });

  it("never invents export or reconcile endpoints", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/payments/admin") {
        return response({ content: [] }) as never;
      }
      if (url === "/api/payments/admin/summary") {
        return response(summary()) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    await listAdminPayments({});

    expect(get.mock.calls.map(([url]) => url)).toEqual([
      "/api/payments/admin",
      "/api/payments/admin/summary",
    ]);
  });
});
