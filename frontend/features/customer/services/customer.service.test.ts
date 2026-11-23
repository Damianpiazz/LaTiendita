import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

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

const orderDto = {
  id: 1042,
  orderNumber: "#1042",
  status: "DELIVERED",
  total: "42900.00",
  createdAt: "2026-03-12T10:00:00Z",
};

const reviewDto = {
  productId: 7,
  title: "Auriculares inalámbricos",
  status: "approved",
  createdAt: "2026-03-05T10:00:00Z",
};

const authHeaders = { Authorization: "Bearer jwt-real" };

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

/** All three profile reads by URL, so one `vi.fn()` can serve them. */
function mockProfileApi(customer: typeof customerDto = customerDto) {
  mocks.get.mockImplementation((url: string) => {
    if (url === "/api/customers/me") {
      return Promise.resolve({ data: customer });
    }
    if (url === "/api/orders") {
      return Promise.resolve({ data: { content: [orderDto], totalElements: 1 } });
    }
    if (url === "/api/reviews/me") {
      return Promise.resolve({ data: [reviewDto] });
    }
    return Promise.reject(new Error(`Unexpected GET ${url}`));
  });
}

describe("getCustomerProfile", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(console, "warn").mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("redirects to login when there is no session", async () => {
    setCookies();

    await expect(getCustomerProfile()).rejects.toThrow("NEXT_REDIRECT");

    expect(mocks.redirect).toHaveBeenCalledWith("/ingresar");
    expect(mocks.get).not.toHaveBeenCalled();
  });

  it("reads identity, own orders and own reviews with the session JWT", async () => {
    setCookies("jwt-real");
    mockProfileApi();

    const profile = await getCustomerProfile();

    expect(mocks.get).toHaveBeenCalledWith("/api/customers/me", { headers: authHeaders });
    expect(mocks.get).toHaveBeenCalledWith("/api/orders", {
      params: { page: 0, size: 100, sort: "createdAt,desc" },
      headers: authHeaders,
    });
    expect(mocks.get).toHaveBeenCalledWith("/api/reviews/me", { headers: authHeaders });
    expect(profile).toMatchObject({
      email: "ana@example.com",
      initials: "AP",
      stats: { orders: 1, totalSpent: 42900, lastPurchase: "12 mar 2026", reviews: 1 },
    });
    expect(profile.orders).toHaveLength(1);
    expect(profile.activity[0].description).toContain("Pedido #1042");
  });

  it("keeps the profile when the optional order and review services fail", async () => {
    setCookies("jwt-real");
    mocks.get.mockImplementation((url: string) =>
      url === "/api/customers/me"
        ? Promise.resolve({ data: customerDto })
        : Promise.reject(new Error("servicio caído")),
    );

    const profile = await getCustomerProfile();

    expect(profile.email).toBe("ana@example.com");
    expect(profile.stats).toEqual({ orders: 0, totalSpent: 0, lastPurchase: "—", reviews: 0 });
    expect(profile.activity).toEqual([]);
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
    let customerCalls = 0;
    mocks.get.mockImplementation((url: string) => {
      if (url === "/api/customers/me") {
        customerCalls += 1;
        return customerCalls === 1
          ? Promise.reject({ response: { status: 404 } })
          : Promise.resolve({ data: newCustomerDto });
      }
      if (url === "/api/orders") {
        return Promise.resolve({ data: { content: [], totalElements: 0 } });
      }
      return Promise.resolve({ data: [] });
    });
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
      { headers: authHeaders },
    );
    expect(profile.email).toBe("bruno@example.com");
    expect(profile.initials).toBe("BD");
  });

  it("re-reads the profile after a 409 instead of crashing the page", async () => {
    const user = {
      id: "authorizer-sub-1",
      email: "ana@example.com",
      givenName: "Ana",
      familyName: "Pérez",
      roles: ["cliente"],
    };
    setCookies("jwt-real", user);
    let customerCalls = 0;
    mocks.get.mockImplementation((url: string) => {
      if (url === "/api/customers/me") {
        customerCalls += 1;
        return customerCalls === 1
          ? Promise.reject({ response: { status: 404 } })
          : Promise.resolve({ data: customerDto });
      }
      if (url === "/api/orders") {
        return Promise.resolve({ data: { content: [], totalElements: 0 } });
      }
      return Promise.resolve({ data: [] });
    });
    mocks.post.mockRejectedValueOnce({ response: { status: 409 } });

    const profile = await getCustomerProfile();

    expect(customerCalls).toBe(2);
    expect(profile.email).toBe("ana@example.com");
  });
});
