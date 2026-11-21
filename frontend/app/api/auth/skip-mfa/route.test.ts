import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { post: mocks.post } }));

import { POST } from "./route";

function skipRequest(): Request {
  return new Request("http://localhost:3000/api/auth/skip-mfa", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      origin: "http://localhost:3000",
      cookie: "mfa_session=abc; mfa_session_domain=abc",
    },
    body: JSON.stringify({ email: "cliente@example.com" }),
  });
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
});
