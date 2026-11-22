// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { createElement } from "react";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { AdminClient, AdminClients } from "@/features/admin/types";

const actions = vi.hoisted(() => ({
  listAdminClients: vi.fn(),
  exportAdminClients: vi.fn(),
}));
vi.mock("@/features/admin/actions/client-actions", () => actions);
import { AdminClientsWorkspace } from "./admin-clients-workspace";

const noPurchase: AdminClient = {
  customerId: "c-2", firstName: "Nico", lastName: "Ruiz", name: "Nico Ruiz", initials: "NR",
  email: "nico@mail.com", phone: "555-2222", orderCount: 0, totalSpent: 0,
  lastPurchase: "-", segment: "sin-compras", status: "sin-compras",
};
const clients: AdminClients = {
  items: [noPurchase], pageSize: 8, totalClients: 128,
  newThisMonth: 24, recurring: 41, averageTicket: 27430,
};

function renderWorkspace() {
  return render(createElement(AdminClientsWorkspace, { initialClients: clients }));
}

beforeEach(() => {
  vi.clearAllMocks();
  actions.listAdminClients.mockResolvedValue(clients);
  actions.exportAdminClients.mockResolvedValue({ filename: "clientes.csv", content: "a,b\n" });
});
afterEach(() => cleanup());

describe("T3 independent clients UI probes", () => {
  it("keeps the wireframe copy and badges sin-compras as Inactiva", () => {
    renderWorkspace();
    expect(screen.getByText("Clientes")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Exportar CSV" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Segmentos" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sin compras" })).toBeInTheDocument();
    expect(screen.getAllByText("Inactiva").length).toBeGreaterThan(0);
    expect(screen.getByText("Mostrando 1–1 de 128 clientes")).toBeInTheDocument();
  });

  it("free text search asks the backend for the same term in name, email and phone", async () => {
    renderWorkspace();
    fireEvent.change(screen.getByRole("searchbox"), { target: { value: "555-2222" } });
    await waitFor(() => expect(actions.listAdminClients).toHaveBeenCalledWith(
      expect.objectContaining({ name: "555-2222", email: "555-2222", phone: "555-2222", page: 1 })));
  });

  it("pagination and sort stay 1-based in the UI contract and reset to page 1", async () => {
    renderWorkspace();
    fireEvent.click(screen.getByRole("button", { name: "2" }));
    await waitFor(() => expect(actions.listAdminClients).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 2 })));
    fireEvent.click(screen.getByRole("button", { name: "Recurrentes" }));
    await waitFor(() => expect(actions.listAdminClients).toHaveBeenLastCalledWith(
      expect.objectContaining({ segment: "recurrente", page: 1 })));
  });

  it("Segmentos menu offers the distinct sin-compras filter", async () => {
    renderWorkspace();
    fireEvent.click(screen.getByRole("button", { name: "Segmentos" }));
    const menu = screen.getByRole("menu", { name: "Segmentos" });
    fireEvent.click(within(menu).getByRole("menuitem", { name: "Sin compras" }));
    await waitFor(() => expect(actions.listAdminClients).toHaveBeenLastCalledWith(
      expect.objectContaining({ segment: "sin-compras", page: 1 })));
  });
});
