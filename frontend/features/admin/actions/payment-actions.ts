"use server";

import { revalidatePath } from "next/cache";

import { getAdminPayments } from "@/features/admin/services/admin.service";
import type { AdminPayments, AdminPaymentsQuery } from "@/features/admin/types";

const PAYMENTS_PATH = "/admin/pagos";

/**
 * The client workspace reuses the same adapter as the server seed, including
 * its Bearer forwarding and page conversion. No export or reconcile endpoint
 * exists in the frozen contract, so this is the only payment action.
 */
export async function listAdminPayments(
  query: AdminPaymentsQuery,
): Promise<AdminPayments> {
  const payments = await getAdminPayments(query);
  revalidatePath(PAYMENTS_PATH);
  return payments;
}
