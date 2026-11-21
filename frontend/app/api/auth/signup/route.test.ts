import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { post: mocks.post } }));

import { POST } from "./route";

function signupRequest(): Request {
  return new Request("http://localhost:3000/api/auth/signup", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      origin: "http://localhost:3000",
    },
    body: JSON.stringify({
      name: "Ana Pérez",
      email: "nueva@example.com",
      password: "ClaveSegura123!",
    }),
  });
}

describe("signup BFF route", () => {
  beforeEach(() => vi.clearAllMocks());

  it("maps the wireframe name into Authorizer fields and stores the JWT", async () => {
    mocks.post.mockResolvedValue({
      data: {
        access_token: "jwt-real",
        expires_in: 3600,
        user: { id: "user-1", email: "nueva@example.com", roles: ["cliente"] },
      },
    });

    const response = await POST(signupRequest());

    expect(response.status).toBe(200);
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/signup",
      {
        email: "nueva@example.com",
        password: "ClaveSegura123!",
        confirm_password: "ClaveSegura123!",
        given_name: "Ana",
        family_name: "Pérez",
        roles: ["cliente"],
      },
      { headers: { Origin: "http://localhost:3000" } },
    );
    expect(response.headers.get("set-cookie") ?? "").toContain("lt_access_token=jwt-real");
  });

  it("keeps the MFA cookies when Authorizer offers enrollment", async () => {
    mocks.post.mockResolvedValue({
      data: {
        message: "Proceed to mfa setup",
        should_show_totp_screen: true,
        access_token: "",
        user: null,
      },
      headers: {
        "set-cookie": ["mfa_session=abc; Path=/; Domain=localhost; HttpOnly; Secure"],
      },
    });

    const response = await POST(signupRequest());

    expect(response.status).toBe(200);
    expect(await response.json()).toEqual({
      mfaRequired: true,
      email: "nueva@example.com",
    });
    expect(response.headers.get("set-cookie") ?? "").toContain("mfa_session=abc");
  });
});
