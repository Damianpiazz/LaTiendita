import { cookies } from "next/headers";

import type { ShippingAddress, ShippingMethod } from "@/features/shipment/types";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

const HOME_DELIVERY_METHOD: ShippingMethod = {
  id: "envio-a-domicilio",
  name: "Envío a domicilio",
};

/**
 * The store ships to a home address only. This is a fixed business rule, not a
 * mock endpoint dependency.
 */
export async function getShippingMethods(): Promise<ShippingMethod[]> {
  return [HOME_DELIVERY_METHOD];
}

function statusOf(error: unknown): number | undefined {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { status?: number } }).response;
    return typeof response?.status === "number" ? response.status : undefined;
  }

  return undefined;
}

/**
 * Loads the signed-in customer's delivery addresses from shipment-service.
 * Returns an empty list when there is no session so the public checkout can
 * still render the address form.
 */
export async function getShippingAddresses(): Promise<ShippingAddress[]> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!token) {
    return [];
  }

  try {
    const { data } = await httpClient.get<ShippingAddress[]>("/api/shipments/addresses", {
      headers: { Authorization: `Bearer ${token}` },
    });

    return Array.isArray(data) ? data : [];
  } catch (error) {
    const status = statusOf(error);

    if (status === 401 || status === 403) {
      return [];
    }

    throw error;
  }
}
