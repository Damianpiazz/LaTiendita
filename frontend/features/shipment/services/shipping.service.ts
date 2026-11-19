import { cacheLife } from "next/cache";

import type { ShippingMethod } from "@/features/shipment/types";
import { httpClient } from "@/lib/http/client";

export async function getShippingMethods(): Promise<ShippingMethod[]> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<ShippingMethod[]>("/shipping/methods");

  return data;
}
