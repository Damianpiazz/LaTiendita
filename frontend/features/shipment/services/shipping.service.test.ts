import { beforeEach, describe, expect, it, vi } from "vitest";

import type { ShippingAddress } from "@/features/shipment/types";

const mocks = vi.hoisted(() => ({
  get: vi.fn(),
  cookieGet: vi.fn(),
}));

vi.mock("@/lib/http/client", () => ({ httpClient: { get: mocks.get } }));
vi.mock("next/headers", () => ({ cookies: async () => ({ get: mocks.cookieGet }) }));

import { getShippingAddresses, getShippingMethods } from "./shipping.service";

const address: ShippingAddress = {
  id: 10,
  recipientName: "Juana Pérez",
  phone: "11 5555 5555",
  addressLine: "Av. Siempre Viva 742",
  city: "CABA",
  province: "Buenos Aires",
  postalCode: "1425",
  references: "Piso 2",
  isDefault: true,
};

function setToken(token?: string) {
  mocks.cookieGet.mockImplementation((name: string) =>
    name === "lt_access_token" && token ? { value: token } : undefined,
  );
}

describe("shipping service", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns the fixed home delivery method without calling the backend", async () => {
    await expect(getShippingMethods()).resolves.toEqual([
      { id: "envio-a-domicilio", name: "Envío a domicilio" },
    ]);
    expect(mocks.get).not.toHaveBeenCalled();
  });

  it("returns an empty list when there is no session", async () => {
    setToken();

    await expect(getShippingAddresses()).resolves.toEqual([]);
    expect(mocks.get).not.toHaveBeenCalled();
  });

  it("loads addresses from the real shipment endpoint with the session token", async () => {
    setToken("jwt-real");
    mocks.get.mockResolvedValue({ data: [address] });

    await expect(getShippingAddresses()).resolves.toEqual([address]);
    expect(mocks.get).toHaveBeenCalledWith("/api/shipments/addresses", {
      headers: { Authorization: "Bearer jwt-real" },
    });
  });

  it("returns an empty list when the gateway rejects the session", async () => {
    setToken("jwt-real");
    mocks.get.mockRejectedValue({ response: { status: 401 } });

    await expect(getShippingAddresses()).resolves.toEqual([]);
  });

  it("rethrows unexpected backend errors", async () => {
    setToken("jwt-real");
    const error = new Error("boom");
    mocks.get.mockRejectedValue(error);

    await expect(getShippingAddresses()).rejects.toBe(error);
  });
});
