// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({
  login: vi.fn(),
  push: vi.fn(),
  refresh: vi.fn(),
}));

vi.mock("@/features/auth/services/auth.service", () => ({ login: mocks.login }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: mocks.push, refresh: mocks.refresh }),
}));

import { LoginForm } from "./login-form";

describe("LoginForm with real Authorizer session", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("submits email and password and lands on the cart", async () => {
    mocks.login.mockResolvedValue({
      id: "user-1",
      email: "cliente@example.com",
      givenName: "Ana",
      familyName: "Pérez",
      roles: ["cliente"],
    });

    render(<LoginForm />);

    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "cliente@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Contraseña"), {
      target: { value: "secreto" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await waitFor(() =>
      expect(mocks.login).toHaveBeenCalledWith("cliente@example.com", "secreto"),
    );
    expect(mocks.push).toHaveBeenCalledWith("/carrito");
  });

  it("shows the backend error and does not navigate", async () => {
    mocks.login.mockRejectedValue(new Error("Email o contraseña incorrectos"));

    render(<LoginForm />);

    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "cliente@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Contraseña"), {
      target: { value: "mala" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Email o contraseña incorrectos",
    );
    expect(mocks.push).not.toHaveBeenCalled();
  });
});
