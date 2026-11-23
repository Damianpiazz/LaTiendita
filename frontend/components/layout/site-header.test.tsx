// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ logout: vi.fn(), push: vi.fn(), refresh: vi.fn() }));

vi.mock("@/features/cart/components/cart-badge", () => ({
  CartBadge: () => <span>Carrito</span>,
}));
vi.mock("@/features/auth/services/auth.service", () => ({ logout: mocks.logout }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: mocks.push, refresh: mocks.refresh }),
}));

import { SiteHeader } from "./site-header";

describe("SiteHeader session states", () => {
  afterEach(() => cleanup());

  beforeEach(() => vi.clearAllMocks());

  it("keeps the wireframe Ingresar control when anonymous", () => {
    render(<SiteHeader />);

    expect(screen.getByRole("link", { name: "Ingresar" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Salir" })).not.toBeInTheDocument();
  });

  it("shows the account chip and logs out when signed in", async () => {
    render(<SiteHeader account={{ initials: "AP", label: "Ana" }} />);

    expect(screen.getByRole("link", { name: /Ana/ })).toHaveAttribute("href", "/mi-cuenta");

    fireEvent.click(screen.getByRole("button", { name: "Salir" }));

    await waitFor(() => expect(mocks.logout).toHaveBeenCalledOnce());
    expect(mocks.push).toHaveBeenCalledWith("/");
  });
});
