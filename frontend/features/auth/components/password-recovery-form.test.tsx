// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ requestPasswordReset: vi.fn() }));

vi.mock("@/features/auth/services/auth.service", () => ({
  requestPasswordReset: mocks.requestPasswordReset,
}));

import { PasswordRecoveryForm } from "./password-recovery-form";

describe("PasswordRecoveryForm with real Authorizer", () => {
  afterEach(() => cleanup());

  beforeEach(() => vi.clearAllMocks());

  it("sends the email and shows the backend instructions", async () => {
    mocks.requestPasswordReset.mockResolvedValue(
      "If an account exists for this email, a password reset link has been sent.",
    );

    render(<PasswordRecoveryForm />);
    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "cliente@example.com" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Enviar instrucciones" }));

    await waitFor(() =>
      expect(mocks.requestPasswordReset).toHaveBeenCalledWith("cliente@example.com"),
    );
    expect(await screen.findByRole("status")).toHaveTextContent(
      "If an account exists for this email, a password reset link has been sent.",
    );
  });

  it("shows the real backend error", async () => {
    mocks.requestPasswordReset.mockRejectedValue(
      new Error("No se pudo enviar las instrucciones"),
    );

    render(<PasswordRecoveryForm />);
    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "cliente@example.com" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Enviar instrucciones" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "No se pudo enviar las instrucciones",
    );
  });
});
