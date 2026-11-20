import { cacheLife } from "next/cache";

import { env } from "@/config/env";
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

type Page<T> = { content: T[] };

/** Backend `CategoryDto`, plus the frontend-shaped seed served by the mock adapter. */
type CategoryDto = {
  categoryId?: string | number;
  id?: string | number;
  name: string;
  slug: string;
  description?: string | null;
  featured?: boolean;
  productCount?: number;
};

/** The real API serves Spring `Page`; mock mode still serves plain arrays. */
function unwrapPage<T>(payload: Page<T> | T[]): T[] {
  return Array.isArray(payload) ? payload : payload.content;
}

/**
 * Categories list and the row the detail card opens.
 *
 * In real mode the admin reads the public catalog so the same category ids back
 * both the admin toggle and the Home grid. Mock mode keeps the wireframe seed.
 */
export async function getAdminCategories(): Promise<AdminCategories> {
  "use cache";
  cacheLife("hours");

  if (env.useMocks) {
    const { data } = await httpClient.get<AdminCategories>("/admin/categories");

    return data;
  }

  const { data } = await httpClient.get<Page<CategoryDto> | CategoryDto[]>("/api/categories");
  const items = unwrapPage(data).map((dto) => ({
    id: String(dto.categoryId ?? dto.id),
    name: dto.name,
    slug: dto.slug,
    description: dto.description ?? "",
    productCount: Number(dto.productCount ?? 0),
    lowStockCount: 0,
    featured: Boolean(dto.featured),
  }));

  return {
    items,
    selectedId: items[0]?.id ?? "",
    updatedLabel: "hoy",
  };
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
