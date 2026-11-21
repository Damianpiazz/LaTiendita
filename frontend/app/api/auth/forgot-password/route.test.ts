import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("@/lib/http/client", () => ({ httpClient: { post: mocks.post } }));

import { POST } from "./route";

function recoveryRequest(): Request {
  return new Request("http://localhost:3000/api/auth/forgot-password", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      origin: "http://localhost:3000",
    },
    body: JSON.stringify({ email: "cliente@example.com" }),
  });
}

describe("forgot-password BFF route", () => {
  beforeEach(() => vi.clearAllMocks());

  it("forwards the Origin and returns the real Authorizer message", async () => {
    mocks.post.mockResolvedValue({
      data: {
        message:
          "If an account exists for this email, a password reset link has been sent. Please check your inbox.",
        should_show_mobile_otp_screen: false,
      },
    });

    const response = await POST(recoveryRequest());

    expect(response.status).toBe(200);
    expect(mocks.post).toHaveBeenCalledWith(
      "/api/auth/forgot_password",
      { email: "cliente@example.com" },
      { headers: { Origin: "http://localhost:3000" } },
    );
    expect(await response.json()).toEqual({
      message:
        "If an account exists for this email, a password reset link has been sent. Please check your inbox.",
    });
  });

  it("rejects an empty email before calling Authorizer", async () => {
    const response = await POST(
      new Request("http://localhost:3000/api/auth/forgot-password", {
        method: "POST",
        headers: { "content-type": "application/json", origin: "http://localhost:3000" },
        body: JSON.stringify({ email: "" }),
      }),
    );

    expect(response.status).toBe(400);
    expect(mocks.post).not.toHaveBeenCalled();
  });
});
