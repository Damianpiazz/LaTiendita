import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { post: mocks.post } }));

import { POST } from "./route";

function skipRequest(body: Record<string, unknown> = { email: "cliente@example.com" }): Request {
  return new Request("http://localhost:3000/api/auth/skip-mfa", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      origin: "http://localhost:3000",
      cookie: "mfa_session=abc; mfa_session_domain=abc",
    },
    body: JSON.stringify(body),
  });
}

function fakeJwt(claims: Record<string, unknown>): string {
  const header = Buffer.from(JSON.stringify({ alg: "HS256" })).toString("base64url");
  const payload = Buffer.from(JSON.stringify(claims)).toString("base64url");
  return `${header}.${payload}.sig`;
}

describe("skip-mfa BFF route", () => {
  beforeEach(() => vi.clearAllMocks());

  it("forwards the MFA cookies and Origin, then stores the real JWT", async () => {
    mocks.post.mockResolvedValue({
      data: {
        access_token: "jwt-real",
        expires_in: 3600,
        user: { id: "user-1", email: "cliente@example.com", roles: ["cliente"] },
      },
    });

    const response = await POST(skipRequest());
    const cookies = response.headers.get("set-cookie") ?? "";

    expect(response.status).toBe(200);
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/skip_mfa_setup",
      { email: "cliente@example.com" },
      {
        headers: {
          Origin: "http://localhost:3000",
          Cookie: "mfa_session=abc; mfa_session_domain=abc",
        },
      },
    );
    expect(cookies).toContain("lt_access_token=jwt-real");
    expect(cookies).toContain("HttpOnly");
    expect(await response.json()).toMatchObject({ user: { id: "user-1" } });
  });

  it("re-logins with elevated roles when password is provided and token allows them", async () => {
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

    const response = await POST(
      skipRequest({ email: "cliente@example.com", password: "ClaveSegura123!" }),
    );
    const cookies = response.headers.get("set-cookie") ?? "";

    expect(response.status).toBe(200);
    expect(mocks.post).toHaveBeenNthCalledWith(
      1,
      "/api/auth/skip_mfa_setup",
      { email: "cliente@example.com" },
      {
        headers: {
          Origin: "http://localhost:3000",
          Cookie: "mfa_session=abc; mfa_session_domain=abc",
        },
      },
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
      user: { roles: ["admin", "cliente"] },
    });
  });
});
