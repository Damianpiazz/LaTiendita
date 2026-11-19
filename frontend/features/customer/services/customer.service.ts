import { cacheLife } from "next/cache";

import type { CustomerProfile } from "@/features/customer/types";
import { httpClient } from "@/lib/http/client";

/**
 * Customer account screen: identity, totals, activity, orders and the address
 * and preferences cards, all served by `GET /customer/profile`.
 */
export async function getCustomerProfile(): Promise<CustomerProfile> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<CustomerProfile>("/customer/profile");

  return data;
}
