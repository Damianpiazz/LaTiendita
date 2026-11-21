// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ signup: vi.fn(), push: vi.fn(), refresh: vi.fn() }));

vi.mock("@/features/auth/services/auth.service", () => ({ signup: mocks.signup }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: mocks.push, refresh: mocks.refresh }),
}));

import { RegisterForm } from "./register-form";

function fillWireframeFields() {
  fireEvent.change(screen.getByLabelText("Nombre y apellido"), {
    target: { value: "Ana Pérez" },
  });
  fireEvent.change(screen.getByLabelText("Email"), {
    target: { value: "nueva@example.com" },
  });
  fireEvent.change(screen.getByLabelText("Contraseña"), {
    target: { value: "ClaveSegura123!" },
  });
  fireEvent.change(screen.getByLabelText("Confirmar contraseña"), {
    target: { value: "ClaveSegura123!" },
  });
}

describe("RegisterForm with real Authorizer signup", () => {
  afterEach(() => cleanup());

  beforeEach(() => vi.clearAllMocks());

  it("submits after the terms are accepted and lands on the cart", async () => {
    mocks.signup.mockResolvedValue({ id: "user-2", email: "nueva@example.com" });

    render(<RegisterForm />);
    fillWireframeFields();
    fireEvent.click(screen.getByRole("checkbox"));
    fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));

    await waitFor(() =>
      expect(mocks.signup).toHaveBeenCalledWith({
        name: "Ana Pérez",
        email: "nueva@example.com",
        password: "ClaveSegura123!",
      }),
    );
    expect(mocks.push).toHaveBeenCalledWith("/carrito");
  });

  it("blocks the submit when the terms are not accepted", () => {
    render(<RegisterForm />);
    fillWireframeFields();
    fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));

    expect(screen.getByRole("alert")).toHaveTextContent(
      "Debés aceptar los términos y condiciones",
    );
    expect(mocks.signup).not.toHaveBeenCalled();
  });

  it("shows the real backend error and does not navigate", async () => {
    mocks.signup.mockRejectedValue(new Error("password is not valid"));

    render(<RegisterForm />);
    fillWireframeFields();
    fireEvent.click(screen.getByRole("checkbox"));
    fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("password is not valid");
    expect(mocks.push).not.toHaveBeenCalled();
  });
});
