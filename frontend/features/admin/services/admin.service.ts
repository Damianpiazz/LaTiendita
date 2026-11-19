import { cacheLife } from "next/cache";

import type {
  AdminCategories,
  AdminClients,
  AdminNavigationCounts,
  AdminOrders,
  AdminPayments,
  AdminProducts,
  AdminSummary,
} from "@/features/admin/types";
import { httpClient } from "@/lib/http/client";

/** Dashboard of the admin summary screen. */
export async function getAdminSummary(): Promise<AdminSummary> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminSummary>("/admin/summary");

  return data;
}

/**
 * Shell counters (sidebar badges and the topbar alert).
 *
 * The admin layout reads them on its own because a layout never receives the
 * page props, and every admin screen shares the same shell.
 */
export async function getAdminNavigation(): Promise<AdminNavigationCounts> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminNavigationCounts>("/admin/navigation");

  return data;
}

/** Categories list and the row the detail card opens. */
export async function getAdminCategories(): Promise<AdminCategories> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminCategories>("/admin/categories");

  return data;
}

/** Products table: visible page, store totals and the low-stock threshold. */
export async function getAdminProducts(): Promise<AdminProducts> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminProducts>("/admin/products");

  return data;
}

/** Orders table: visible page, store totals and the status tab counts. */
export async function getAdminOrders(): Promise<AdminOrders> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminOrders>("/admin/orders");

  return data;
}

/** Payments table: visible page, settlement totals and the period KPIs. */
export async function getAdminPayments(): Promise<AdminPayments> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminPayments>("/admin/payments");

  return data;
}

/** Clients table: visible page, store totals and the stats strip figures. */
export async function getAdminClients(): Promise<AdminClients> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<AdminClients>("/admin/clients");

  return data;
}
