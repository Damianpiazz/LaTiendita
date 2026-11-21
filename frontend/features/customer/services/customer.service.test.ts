import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  cookieGet: vi.fn(),
  redirect: vi.fn(),
}));

vi.mock("@/lib/http/client", () => ({ httpClient: { get: mocks.get, post: mocks.post } }));
vi.mock("next/headers", () => ({ cookies: async () => ({ get: mocks.cookieGet }) }));
vi.mock("next/navigation", () => ({
  redirect: (path: string) => {
    mocks.redirect(path);
    throw new Error("NEXT_REDIRECT");
  },
}));

import { encodeUserCookie } from "@/lib/auth/session";
import { getCustomerProfile } from "./customer.service";

const customerDto = {
  id: "d2c4f1a0-1234-4c56-8abc-99ff00112233",
  email: "ana@example.com",
  firstName: "Ana",
  lastName: "Pérez",
  phone: "+54 9 11 5555 0101",
  status: "ACTIVE",
  newsletterOptIn: false,
  newsletterConsentAt: null,
  createdAt: "2025-04-02T10:00:00Z",
};

function setCookies(token?: string, user?: Parameters<typeof encodeUserCookie>[0]) {
  mocks.cookieGet.mockImplementation((name: string) => {
    if (name === "lt_access_token" && token) {
      return { value: token };
    }
    if (name === "lt_user" && user) {
      return { value: encodeUserCookie(user) };
    }
    return undefined;
  });
}

describe("getCustomerProfile", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("redirects to login when there is no session", async () => {
    setCookies();

    await expect(getCustomerProfile()).rejects.toThrow("NEXT_REDIRECT");

    expect(mocks.redirect).toHaveBeenCalledWith("/ingresar");
    expect(mocks.get).not.toHaveBeenCalled();
  });

  it("reads the authenticated customer with the session JWT", async () => {
    setCookies("jwt-real");
    mocks.get.mockResolvedValue({ data: customerDto });

    const profile = await getCustomerProfile();

    expect(mocks.get).toHaveBeenCalledWith("/api/customers/me", {
      headers: { Authorization: "Bearer jwt-real" },
    });
    expect(profile).toMatchObject({ email: "ana@example.com", initials: "AP" });
  });

  it("provisions the customer once when the backend has no record yet", async () => {
    const user = {
      id: "authorizer-sub-1",
      email: "bruno@example.com",
      givenName: "Bruno",
      familyName: "Díaz",
      roles: ["cliente"],
    };
    setCookies("jwt-real", user);
    const newCustomerDto = {
      ...customerDto,
      id: "99999999-9999-4999-8999-999999999999",
      email: "bruno@example.com",
      firstName: "Bruno",
      lastName: "Díaz",
    };
    mocks.get.mockRejectedValueOnce({ response: { status: 404 } });
    mocks.post.mockResolvedValue({ data: newCustomerDto });

    const profile = await getCustomerProfile();

    expect(mocks.post).toHaveBeenCalledWith(
      "/api/customers",
      {
        email: "bruno@example.com",
        firstName: "Bruno",
        lastName: "Díaz",
        phone: "",
      },
      { headers: { Authorization: "Bearer jwt-real" } },
    );
    expect(profile.email).toBe("bruno@example.com");
    expect(profile.initials).toBe("BD");
  });
});
