import { cookies } from "next/headers";

import { env } from "@/config/env";
import {
  mapAdminProductSummary,
  mapAdminProduct,
  type AdminProductApi,
  type AdminProductSummaryApi,
} from "@/features/admin/helpers/admin-products.mapper";
import {
  mapAdminSummary,
  type AdminLowStockApi,
  type AdminOrderSummaryApi,
  type AdminPaymentSummaryApi,
} from "@/features/admin/helpers/admin-summary.mapper";
import type {
  AdminCategories,
  AdminProductsQuery,
  AdminClients,
  AdminNavigationCounts,
  AdminOrders,
  AdminPayments,
  AdminProducts,
  AdminSummary,
} from "@/features/admin/types";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

const LOW_STOCK_THRESHOLD = 5;
const LOW_STOCK_SIZE = 5;

/** The wireframe draws eight rows, newest first. */
export const ADMIN_PRODUCTS_PAGE_SIZE = 8;
export const ADMIN_PRODUCTS_DEFAULT_SORT = "id,desc";

async function adminAuthHeaders(): Promise<Record<string, string>> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** Dashboard of the admin summary screen. */
export async function getAdminSummary(): Promise<AdminSummary> {
  if (env.useMocks) {
    const { data } = await httpClient.get<AdminSummary>("/admin/summary");
    return data;
  }

  const headers = await adminAuthHeaders();
  const [orders, payments, lowStock] = await Promise.all([
    httpClient
      .get<AdminOrderSummaryApi>("/api/orders/admin/summary", { headers })
      .then((response) => response.data),
    httpClient
      .get<AdminPaymentSummaryApi>("/api/payments/admin/summary", { headers })
      .then((response) => response.data),
    httpClient
      .get<AdminLowStockApi>("/api/products/admin/low-stock", {
        params: { threshold: LOW_STOCK_THRESHOLD, size: LOW_STOCK_SIZE },
        headers,
      })
      .then((response) => response.data),
  ]);

  return mapAdminSummary({ orders, payments, lowStock });
}

/**
 * Shell counters (sidebar badges and the topbar alert).
 *
 * The admin layout reads them on its own because a layout never receives the
 * page props, and every admin screen shares the same shell.
 */
export async function getAdminNavigation(): Promise<AdminNavigationCounts> {
  if (env.useMocks) {
    const { data } = await httpClient.get<AdminNavigationCounts>("/admin/navigation");
    return data;
  }

  const headers = await adminAuthHeaders();
  const [orders, payments, lowStock] = await Promise.all([
    httpClient
      .get<AdminOrderSummaryApi>("/api/orders/admin/summary", { headers })
      .then((response) => response.data),
    httpClient
      .get<AdminPaymentSummaryApi>("/api/payments/admin/summary", { headers })
      .then((response) => response.data),
    httpClient
      .get<AdminLowStockApi>("/api/products/admin/low-stock", {
        params: { threshold: LOW_STOCK_THRESHOLD, size: 1 },
        headers,
      })
      .then((response) => response.data),
  ]);

  return {
    pendingOrders: orders.pendingOrders,
    payments: payments.paymentsInReview,
    alerts: lowStock.totalLowStock,
  };
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

/**
 * Products table: visible page, real store totals and the low-stock threshold.
 *
 * This screen has no mock route: the admin page and its summary are always read
 * in parallel with the Bearer token, even when other admin screens use seeds.
 */
export async function getAdminProducts(
  query: AdminProductsQuery = {},
): Promise<AdminProducts> {
  const headers = await adminAuthHeaders();
  const size = query.size ?? ADMIN_PRODUCTS_PAGE_SIZE;
  const uiPage = query.page ?? 1;
  const params: Record<string, string | number> = {
    page: Math.max(0, uiPage - 1),
    size,
    sort: query.sort ?? ADMIN_PRODUCTS_DEFAULT_SORT,
  };
  if (query.q) params.q = query.q;
  if (query.status && query.status !== "all") params.status = query.status;
  if (query.categoryId) params.categoryId = query.categoryId;

  const summaryParams: Record<string, string> = {};
  if (query.q) summaryParams.q = query.q;
  if (query.categoryId) summaryParams.categoryId = query.categoryId;

  const [list, summary] = await Promise.all([
    httpClient.get<Page<AdminProductApi> | AdminProductApi[]>("/api/products/admin", {
      params,
      headers,
    }),
    httpClient.get<AdminProductSummaryApi>("/api/products/admin/summary", {
      params: summaryParams,
      headers,
    }),
  ]);

  return {
    items: unwrapPage(list.data).map(mapAdminProduct),
    pageSize: size,
    ...mapAdminProductSummary(summary.data),
  };
}

/** Orders table: visible page, store totals and the status tab counts. */
export async function getAdminOrders(): Promise<AdminOrders> {
  const { data } = await httpClient.get<AdminOrders>("/admin/orders");

  return data;
}

/** Payments table: visible page, settlement totals and the period KPIs. */
export async function getAdminPayments(): Promise<AdminPayments> {
  const { data } = await httpClient.get<AdminPayments>("/admin/payments");

  return data;
}

/** Clients table: visible page, store totals and the stats strip figures. */
export async function getAdminClients(): Promise<AdminClients> {
  const { data } = await httpClient.get<AdminClients>("/admin/clients");

  return data;
}
