// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type {
  AdminClient,
  AdminClientDetail,
  AdminClients,
} from "@/features/admin/types";

const actions = vi.hoisted(() => ({
  listAdminClients: vi.fn(),
  exportAdminClients: vi.fn(),
  getAdminClientDetail: vi.fn(),
}));

vi.mock("@/features/admin/actions/client-actions", () => actions);

import { AdminClientsWorkspace } from "./admin-clients-workspace";

const client: AdminClient = {
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
  customerId: "c-1",
  status: "activa",
  newsletterOptIn: true,
};

function renderWorkspace(initialDetail: AdminClientDetail | null = null) {
  return render(
    <AdminClientsWorkspace initialClients={clients} detail={initialDetail} />,
  );
}

function defaults(overrides: Record<string, unknown> = {}) {
  return { page: 1, size: 8, sort: "lastPurchaseAt,desc", ...overrides };
}

describe("AdminClientsWorkspace", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    actions.listAdminClients.mockResolvedValue(clients);
    actions.exportAdminClients.mockResolvedValue({
      filename: "clientes.csv",
      content: "customerId,name\nc-1,Ana Torres\n",
    });
    actions.getAdminClientDetail.mockResolvedValue(detail);
  });

  afterEach(() => {
    cleanup();
  });

  it("keeps the wireframe copy and derives the range from real totals", () => {
    renderWorkspace();

    expect(
      screen.getByText(/128 clientes · 24 nuevos este mes/),
    ).toBeInTheDocument();
    expect(
      screen.getByText("Mostrando 1–1 de 128 clientes"),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Ordenar: Última compra ↓" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("searchbox")).toHaveAttribute(
      "placeholder",
      "Buscar por nombre, email o teléfono...",
    );
  });

  it("searches by name, email and phone through the real list action", async () => {
    renderWorkspace();

    fireEvent.change(screen.getByRole("searchbox"), {
      target: { value: "ana" },
    });

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(
          defaults({ name: "ana", email: "ana", phone: "ana" }),
        ),
      ),
    );
  });

  it("maps the Nuevos pill to the nueva segment", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Nuevos" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ segment: "nueva" })),
      ),
    );
    expect(screen.getByRole("button", { name: "Nuevos" })).toHaveAttribute(
      "aria-current",
      "true",
    );
  });

  it("maps the Recurrentes pill to the recurrente segment", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Recurrentes" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ segment: "recurrente" })),
      ),
    );
  });

  it("maps the Sin compras pill to the sin-compras segment", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Sin compras" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ segment: "sin-compras" })),
      ),
    );
  });

  it("clears the segment when Todos is selected again", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Nuevos" }));
    fireEvent.click(screen.getByRole("button", { name: "Todos" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults()),
      ),
    );
    expect(screen.getByRole("button", { name: "Todos" })).toHaveAttribute(
      "aria-current",
      "true",
    );
  });

  it("toggles the sort direction from the control", async () => {
    renderWorkspace();

    fireEvent.click(
      screen.getByRole("button", { name: "Ordenar: Última compra ↓" }),
    );

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ sort: "lastPurchaseAt,asc" })),
      ),
    );
    expect(
      screen.getByRole("button", { name: "Ordenar: Última compra ↑" }),
    ).toBeInTheDocument();
  });

  it("requests the selected page through the shared pagination", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "2" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ page: 2 })),
      ),
    );
    expect(
      screen.getByText("Mostrando 9–9 de 128 clientes"),
    ).toBeInTheDocument();
  });

  it("exports the filtered CSV and downloads it", async () => {
    const createObjectURL = vi.fn(() => "blob:clientes");
    const revokeObjectURL = vi.fn();
    Object.defineProperty(URL, "createObjectURL", {
      value: createObjectURL,
      configurable: true,
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      value: revokeObjectURL,
      configurable: true,
    });
    const click = vi
      .spyOn(HTMLAnchorElement.prototype, "click")
      .mockImplementation(() => {});

    renderWorkspace();
    fireEvent.click(screen.getByRole("button", { name: "Exportar CSV" }));

    await waitFor(() =>
      expect(actions.exportAdminClients).toHaveBeenCalledWith(
        expect.objectContaining(defaults()),
      ),
    );
    expect(createObjectURL).toHaveBeenCalled();
    expect(click).toHaveBeenCalled();
    click.mockRestore();
  });

  it("changes the active segment from the Segmentos menu", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Segmentos" }));
    const menu = screen.getByRole("menu");
    fireEvent.click(within(menu).getByRole("menuitem", { name: "Recurrentes" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ segment: "recurrente" })),
      ),
    );
    expect(
      screen.getByRole("button", { name: "Todos" }),
    ).not.toHaveAttribute("aria-current");
    expect(
      screen.getByRole("button", { name: "Recurrentes" }),
    ).toHaveAttribute("aria-current", "true");
  });

  it("opens the server-loaded detail through the Ver link", () => {
    renderWorkspace(detail);

    expect(screen.getByRole("link", { name: "Ver →" })).toHaveAttribute(
      "href",
      "/admin/clientes?customer=c-1",
    );
    const dialog = screen.getByRole("dialog");
    expect(within(dialog).getByText("Ana Torres")).toBeInTheDocument();
    expect(
      within(dialog).getByText("+54 11 5555-1038"),
    ).toBeInTheDocument();
    expect(within(dialog).getByText("Activa")).toBeInTheDocument();
  });

  it("maps the Segmentos menu Sin compras option to the sin-compras segment", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Segmentos" }));
    const menu = screen.getByRole("menu");
    fireEvent.click(within(menu).getByRole("menuitem", { name: "Sin compras" }));

    await waitFor(() =>
      expect(actions.listAdminClients).toHaveBeenLastCalledWith(
        expect.objectContaining(defaults({ segment: "sin-compras" })),
      ),
    );
  });

  it("shows the Inactiva badge for a sin-compras row", () => {
    render(
      <AdminClientsWorkspace
        initialClients={{
          ...clients,
          items: [{ ...client, segment: "sin-compras", status: "sin-compras" }],
        }}
      />,
    );

    const row = screen.getByRole("row", { name: /Ana Torres/ });
    expect(within(row).getByText("Inactiva")).toBeInTheDocument();
  });
});
