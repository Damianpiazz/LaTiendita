import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { post: mocks.post } }));

import { POST } from "./route";

function loginRequest(): Request {
  return new Request("http://localhost:3000/api/auth/login", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      origin: "http://localhost:3000",
    },
    body: JSON.stringify({ email: "cliente@example.com", password: "ClaveSegura123!" }),
  });
}

function fakeJwt(claims: Record<string, unknown>): string {
  const header = Buffer.from(JSON.stringify({ alg: "HS256" })).toString("base64url");
  const payload = Buffer.from(JSON.stringify(claims)).toString("base64url");
  return `${header}.${payload}.sig`;
}

describe("login BFF route", () => {
  beforeEach(() => vi.clearAllMocks());

  it("forwards the browser Origin and stores the JWT in an httpOnly cookie", async () => {
    mocks.post.mockResolvedValue({
      data: {
        access_token: "jwt-real",
        expires_in: 3600,
        user: { id: "user-1", email: "cliente@example.com", roles: ["cliente"] },
      },
    });

    const response = await POST(loginRequest());
    const cookies = response.headers.get("set-cookie") ?? "";

    expect(response.status).toBe(200);
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/login",
      { email: "cliente@example.com", password: "ClaveSegura123!" },
      { headers: { Origin: "http://localhost:3000" } },
    );
    expect(cookies).toContain("lt_access_token=jwt-real");
    expect(cookies).toContain("HttpOnly");
    expect(await response.json()).toMatchObject({ user: { id: "user-1" } });
  });

  it("keeps the MFA cookies and signals the offer instead of failing", async () => {
    mocks.post.mockResolvedValue({
      data: {
        message: "Proceed to mfa setup",
        should_show_totp_screen: true,
        access_token: "",
        expires_in: "0",
        user: null,
      },
      headers: {
        "set-cookie": [
          "mfa_session=abc; Path=/; Domain=localhost; HttpOnly; Secure; SameSite=None",
          "mfa_session_domain=abc; Path=/; Domain=localhost; HttpOnly; Secure; SameSite=None",
        ],
      },
    });

    const response = await POST(loginRequest());
    const cookies = response.headers.get("set-cookie") ?? "";

    expect(response.status).toBe(200);
    expect(await response.json()).toEqual({
      mfaRequired: true,
      email: "cliente@example.com",
    });
    expect(cookies).toContain("mfa_session=abc");
    expect(cookies).not.toContain("lt_access_token=");
  });

  it("re-logins requesting allowed roles when the token is missing them", async () => {
    const clienteToken = fakeJwt({
      roles: ["cliente"],
      allowed_roles: ["admin", "cliente"],
    });
    const adminToken = fakeJwt({
      roles: ["admin", "cliente"],
      allowed_roles: ["admin", "cliente"],
    });

    mocks.post
      .mockResolvedValueOnce({
        data: {
          access_token: clienteToken,
          expires_in: 3600,
          user: { id: "user-1", email: "cliente@example.com", roles: ["cliente"] },
        },
      })
      .mockResolvedValueOnce({
        data: {
          access_token: adminToken,
          expires_in: 3600,
          user: { id: "user-1", email: "cliente@example.com", roles: ["admin", "cliente"] },
        },
      });

    const response = await POST(loginRequest());
    const cookies = response.headers.get("set-cookie") ?? "";

    expect(response.status).toBe(200);
    expect(mocks.post).toHaveBeenCalledTimes(2);
    expect(mocks.post).toHaveBeenNthCalledWith(
      1,
      "/api/auth/login",
      { email: "cliente@example.com", password: "ClaveSegura123!" },
      { headers: { Origin: "http://localhost:3000" } },
    );
    expect(mocks.post).toHaveBeenNthCalledWith(
      2,
      "/api/auth/login",
      {
        email: "cliente@example.com",
        password: "ClaveSegura123!",
        roles: ["admin", "cliente"],
      },
      { headers: { Origin: "http://localhost:3000" } },
    );
    expect(cookies).toContain(`lt_access_token=${adminToken}`);
    expect(await response.json()).toMatchObject({
      user: { id: "user-1", roles: ["admin", "cliente"] },
    });
  });
});
