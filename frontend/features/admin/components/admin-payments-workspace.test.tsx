// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { AdminPayments } from "@/features/admin/types";

const actions = vi.hoisted(() => ({
  listAdminPayments: vi.fn(),
}));

vi.mock("@/features/admin/actions/payment-actions", () => actions);

import { AdminPaymentsWorkspace } from "./admin-payments-workspace";

const payments: AdminPayments = {
  items: Array.from({ length: 8 }, (_, index) => ({
    id: `#${812 + index}`,
    orderId: "#1042",
    customer: `user-${index + 1}`,
    ...(index === 1 ? { email: "cliente@mail.com" } : {}),
    provider: "Mercado Pago",
    method: "Tarjeta de crédito · Visa",
    paidAt: "10/11 · 09:42",
    amount: 45998,
    status: index === 0 ? "pendiente" : "aprobado",
  })),
  pageSize: 8,
  totalPayments: 48,
  approvedCount: 34,
  pendingCount: 11,
  rejectedCount: 3,
  approvedVolume: 1284950,
  pendingVolume: 412370,
  rejectedVolume: 67994,
  approvalRate: 91,
  approvalAttempts: 37,
  rangeLabel: "Últimos 30 días",
};

function renderWorkspace(initial: AdminPayments = payments) {
  return render(<AdminPaymentsWorkspace initialPayments={initial} />);
}

function defaults(overrides: Record<string, unknown> = {}) {
  return { page: 1, size: 8, sort: "paidAt,desc", ...overrides };
}

describe("AdminPaymentsWorkspace", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    actions.listAdminPayments.mockResolvedValue(payments);
  });

  afterEach(() => {
    cleanup();
  });

  it("renders the wireframe copy and the summary figures returned by the adapter", () => {
    renderWorkspace();

    expect(
      screen.getByText("48 pagos · 34 acreditados · 3 rechazados por revisar"),
    ).toBeInTheDocument();
    expect(screen.getByText("34 de 37 intentos")).toBeInTheDocument();
    expect(screen.getByText("91%")).toBeInTheDocument();
    expect(screen.getByText("Rango:")).toBeInTheDocument();
    expect(screen.getByText("Últimos 30 días")).toBeInTheDocument();
    expect(screen.getByText("Movimientos")).toBeInTheDocument();
    expect(screen.getByText("Todos los pagos procesados por Mercado Pago")).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText("Buscar por pago, pedido o cliente..."),
    ).toBeInTheDocument();
    expect(screen.getByText("Ordenar:")).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Más recientes ↓" })).toBeInTheDocument();
    expect(screen.getAllByText("Ver →")).toHaveLength(8);
    expect(screen.getByText("Mostrando 1–8 de 48 pagos")).toBeInTheDocument();
    expect(actions.listAdminPayments).not.toHaveBeenCalled();
  });

  it("shows the customer id and omits the email line when the gateway has no email", () => {
    renderWorkspace();

    const customerCell = screen.getByText("user-1").closest('[role="cell"]');
    expect(customerCell).toHaveTextContent("user-1");
    expect(customerCell).not.toHaveTextContent("@");
    expect(screen.getByText("cliente@mail.com")).toBeInTheDocument();
  });

  it("searches by payment, order or customer through the real list action", async () => {
    renderWorkspace();

    fireEvent.change(screen.getByRole("searchbox", { name: "Buscar pagos" }), {
      target: { value: "1042" },
    });

    await waitFor(() =>
      expect(actions.listAdminPayments).toHaveBeenCalledWith(
        expect.objectContaining(defaults({ q: "1042" })),
      ),
    );
  });

  it("filters by status pill with the settlement vocabulary", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: /Aprobados/ }));

    await waitFor(() =>
      expect(actions.listAdminPayments).toHaveBeenCalledWith(
        expect.objectContaining(defaults({ status: "aprobado" })),
      ),
    );
    expect(screen.getByRole("button", { name: /Aprobados/ })).toHaveAttribute(
      "aria-current",
      "true",
    );
  });

  it("changes the sort through the real list action", async () => {
    renderWorkspace();

    fireEvent.change(screen.getByLabelText("Ordenar"), {
      target: { value: "paidAt,asc" },
    });

    await waitFor(() =>
      expect(actions.listAdminPayments).toHaveBeenCalledWith(
        expect.objectContaining(defaults({ sort: "paidAt,asc" })),
      ),
    );
  });

  it("changes page through the shared pagination control", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "2" }));

    await waitFor(() =>
      expect(actions.listAdminPayments).toHaveBeenCalledWith(
        expect.objectContaining(defaults({ page: 2 })),
      ),
    );
  });

  it("reports a load error through the public interface without clearing the table", async () => {
    actions.listAdminPayments.mockRejectedValueOnce(new Error("gateway down"));
    renderWorkspace();

    fireEvent.change(screen.getByRole("searchbox", { name: "Buscar pagos" }), {
      target: { value: "sin coincidencias" },
    });

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "No se pudo cargar la lista de pagos.",
    );
    expect(screen.getByText("#812")).toBeInTheDocument();
  });
});
