import type { ShippingMethod } from "@/features/shipment/types";
import { httpClient } from "@/lib/http/client";

export async function getShippingMethods(): Promise<ShippingMethod[]> {
  const { data } = await httpClient.get<ShippingMethod[]>("/shipping/methods");

  return data;
}
