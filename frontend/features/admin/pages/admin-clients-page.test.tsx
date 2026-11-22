import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({
  service: {
    getAdminClients: vi.fn(),
    ADMIN_CLIENTS_PAGE_SIZE: 8,
    ADMIN_CLIENTS_DEFAULT_SORT: "lastPurchaseAt,desc",
  },
  actions: { getAdminClientDetail: vi.fn() },
  workspace: vi.fn(),
}));

vi.mock("@/features/admin/services/admin.service", () => mocks.service);
vi.mock("@/features/admin/actions/client-actions", () => mocks.actions);
vi.mock("@/features/admin/components/admin-clients-workspace", () => ({
  AdminClientsWorkspace: (props: unknown) => {
    mocks.workspace(props);
    return null;
  },
}));

import type { AdminClientDetail, AdminClients } from "@/features/admin/types";

import { AdminClientsPage } from "./admin-clients-page";

const client = {
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

const clients: AdminClients = {
  items: [client],
  pageSize: 8,
  totalClients: 128,
  newThisMonth: 24,
  recurring: 41,
  averageTicket: 27430,
};

const detail: AdminClientDetail = {
  ...client,
  status: "activa",
};

type SearchParams = Record<string, string | string[] | undefined>;

function propsOf(element: unknown): Record<string, unknown> {
  return (element as { props: Record<string, unknown> }).props;
}

describe("AdminClientsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.service.getAdminClients.mockResolvedValue(clients);
    mocks.actions.getAdminClientDetail.mockResolvedValue(detail);
  });

  it("loads the list with parsed query params and no customer detail", async () => {
    const query: SearchParams = {
      page: "2",
      size: "8",
      sort: "name,asc",
      name: "Ana",
      segment: "recurrente",
    };

    const element = await AdminClientsPage({
      searchParams: Promise.resolve(query),
    });

    expect(mocks.service.getAdminClients).toHaveBeenCalledWith({
      page: 2,
      size: 8,
      sort: "name,asc",
      name: "Ana",
      segment: "recurrente",
    });
    expect(mocks.actions.getAdminClientDetail).not.toHaveBeenCalled();
    const props = propsOf(element);
    expect(props).toEqual({
      initialClients: clients,
      initialQuery: {
        page: 2,
        size: 8,
        sort: "name,asc",
        name: "Ana",
        segment: "recurrente",
      },
      detail: null,
    });
    expect(JSON.stringify(props)).not.toContain("Bearer");
  });

  it("loads the detail on the server from the customer search param", async () => {
    const element = await AdminClientsPage({
      searchParams: Promise.resolve({ customer: "c-1" }),
    });

    expect(mocks.actions.getAdminClientDetail).toHaveBeenCalledWith(
      "c-1",
      client,
    );
    const props = propsOf(element);
    expect(props.detail).toEqual(detail);
    expect(props.initialQuery).toEqual({
      page: 1,
      size: 8,
      sort: "lastPurchaseAt,desc",
    });
    expect(JSON.stringify(props)).not.toContain("Authorization");
  });

  it("forwards phone and the sin-compras segment exactly", async () => {
    await AdminClientsPage({
      searchParams: Promise.resolve({
        phone: "5555",
        segment: "sin-compras",
      }),
    });

    expect(mocks.service.getAdminClients).toHaveBeenCalledWith({
      page: 1,
      size: 8,
      sort: "lastPurchaseAt,desc",
      phone: "5555",
      segment: "sin-compras",
    });
  });
});
