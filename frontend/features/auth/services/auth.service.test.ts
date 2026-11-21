import { AxiosError, AxiosHeaders } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { post: mocks.post } }));

import { login, logout, requestPasswordReset, signup } from "./auth.service";

describe("auth service", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("sends credentials to the Next BFF with same-origin baseURL", async () => {
    mocks.post.mockResolvedValue({
      data: {
        user: {
          id: "user-1",
          email: "cliente@example.com",
          givenName: "Ana",
          familyName: "Pérez",
          roles: ["cliente"],
        },
      },
    });

    await expect(login("cliente@example.com", "secreto")).resolves.toEqual({
      id: "user-1",
      email: "cliente@example.com",
      givenName: "Ana",
      familyName: "Pérez",
      roles: ["cliente"],
    });
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/login",
      { email: "cliente@example.com", password: "secreto" },
      { baseURL: "" },
    );
  });

  it("surfaces the BFF error message on bad credentials", async () => {
    const error = new AxiosError("bad credentials");
    error.response = {
      data: { message: "Email o contraseña incorrectos" },
      status: 401,
      statusText: "Unauthorized",
      headers: {},
      config: { headers: new AxiosHeaders() },
    };
    mocks.post.mockRejectedValue(error);

    await expect(login("cliente@example.com", "mala")).rejects.toThrow(
      "Email o contraseña incorrectos",
    );
  });

  it("clears the server session on logout", async () => {
    mocks.post.mockResolvedValue({ data: { ok: true } });

    await logout();

    expect(mocks.post).toHaveBeenCalledWith("/api/auth/logout", undefined, { baseURL: "" });
  });

  it("completes the MFA offer automatically and returns the user", async () => {
    mocks.post
      .mockResolvedValueOnce({ data: { mfaRequired: true, email: "cliente@example.com" } })
      .mockResolvedValueOnce({
        data: {
          user: {
            id: "user-1",
            email: "cliente@example.com",
            givenName: "Ana",
            familyName: "Pérez",
            roles: ["cliente"],
          },
        },
      });

    await expect(login("cliente@example.com", "secreto")).resolves.toMatchObject({
      id: "user-1",
    });
    expect(mocks.post).toHaveBeenNthCalledWith(
      2,
      "/api/auth/skip-mfa",
      { email: "cliente@example.com" },
      { baseURL: "" },
    );
  });

  it("sends the wireframe registration fields to the signup BFF", async () => {
    mocks.post.mockResolvedValue({
      data: { user: { id: "user-2", email: "nueva@example.com", roles: ["cliente"] } },
    });

    await expect(
      signup({
        name: "Ana Pérez",
        email: "nueva@example.com",
        password: "ClaveSegura123!",
      }),
    ).resolves.toMatchObject({ id: "user-2" });
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/signup",
      {
        name: "Ana Pérez",
        email: "nueva@example.com",
        password: "ClaveSegura123!",
      },
      { baseURL: "" },
    );
  });

  it("completes the MFA offer after a successful signup", async () => {
    mocks.post
      .mockResolvedValueOnce({ data: { mfaRequired: true, email: "nueva@example.com" } })
      .mockResolvedValueOnce({
        data: { user: { id: "user-3", email: "nueva@example.com", roles: ["cliente"] } },
      });

    await expect(
      signup({
        name: "Ana Pérez",
        email: "nueva@example.com",
        password: "ClaveSegura123!",
      }),
    ).resolves.toMatchObject({ id: "user-3" });
    expect(mocks.post).toHaveBeenNthCalledWith(
      2,
      "/api/auth/skip-mfa",
      { email: "nueva@example.com" },
      { baseURL: "" },
    );
  });

  it("surfaces the real Authorizer password policy error", async () => {
    const error = new AxiosError("bad password");
    error.response = {
      data: { message: "password is not valid. It needs to be at least 6 characters long and contain at least one number, one uppercase letter, one lowercase letter and one special character" },
      status: 400,
      statusText: "Bad Request",
      headers: {},
      config: { headers: new AxiosHeaders() },
    };
    mocks.post.mockRejectedValue(error);

    await expect(
      signup({ name: "Ana Pérez", email: "nueva@example.com", password: "mala" }),
    ).rejects.toThrow("password is not valid");
  });

  it("requests password reset instructions through the BFF", async () => {
    mocks.post.mockResolvedValue({
      data: { message: "If an account exists for this email, a password reset link has been sent." },
    });

    await expect(requestPasswordReset("cliente@example.com")).resolves.toBe(
      "If an account exists for this email, a password reset link has been sent.",
    );
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/forgot-password",
      { email: "cliente@example.com" },
      { baseURL: "" },
    );
  });

  it("surfaces the recovery backend error", async () => {
    const error = new AxiosError("recovery failed");
    error.response = {
      data: { message: "No se pudo enviar las instrucciones" },
      status: 502,
      statusText: "Bad Gateway",
      headers: {},
      config: { headers: new AxiosHeaders() },
    };
    mocks.post.mockRejectedValue(error);

    await expect(requestPasswordReset("cliente@example.com")).rejects.toThrow(
      "No se pudo enviar las instrucciones",
    );
  });
});
