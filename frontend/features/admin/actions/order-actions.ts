"use server";

import { getAdminOrders } from "@/features/admin/services/admin.service";
import type { AdminOrders, AdminOrdersQuery } from "@/features/admin/types";

export async function listAdminOrders(
  query: AdminOrdersQuery = {},
): Promise<AdminOrders> {
  return getAdminOrders(query);
}
