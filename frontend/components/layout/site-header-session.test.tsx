// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ get: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { get: mocks.get } }));
vi.mock("@/features/cart/components/cart-badge", () => ({
  CartBadge: () => <span>Carrito</span>,
}));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), refresh: vi.fn() }),
}));

import { SiteHeaderSession } from "./site-header-session";

describe("SiteHeaderSession", () => {
  afterEach(() => cleanup());

  beforeEach(() => vi.clearAllMocks());

  it("renders the anonymous Ingresar control when there is no session", async () => {
    mocks.get.mockResolvedValue({ data: { authenticated: false, user: null } });

    render(<SiteHeaderSession />);

    expect(await screen.findByRole("link", { name: "Ingresar" })).toBeInTheDocument();
    expect(mocks.get).toHaveBeenCalledWith("/api/auth/session", { baseURL: "" });
  });

  it("renders the account chip from the real session response", async () => {
    mocks.get.mockResolvedValue({
      data: {
        authenticated: true,
        user: {
          id: "user-1",
          email: "cliente@example.com",
          givenName: "Ana",
          familyName: "Pérez",
          roles: ["cliente"],
        },
      },
    });

    render(<SiteHeaderSession />);

    expect(await screen.findByRole("link", { name: /Ana/ })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Salir" })).toBeInTheDocument();
  });
});
