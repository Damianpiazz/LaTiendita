import { cookies } from "next/headers";

import { env } from "@/config/env";
import {
  mapAdminProductSummary,
  mapAdminProduct,
  type AdminProductApi,
  type AdminProductSummaryApi,
} from "@/features/admin/helpers/admin-products.mapper";
import {
  mapAdminOrders,
  type AdminOrderApi,
  type AdminOrderStatsApi,
} from "@/features/admin/helpers/admin-orders.mapper";
import {
  mapAdminPayment,
  mapAdminPaymentSummary,
  type AdminPaymentApi,
  type AdminPaymentSummaryApi as AdminPaymentsSummaryApi,
} from "@/features/admin/helpers/admin-payments.mapper";
import {
  mapAdminSummary,
  type AdminLowStockApi,
  type AdminOrderSummaryApi,
  type AdminPaymentSummaryApi,
} from "@/features/admin/helpers/admin-summary.mapper";
import {
  ADMIN_CLIENTS_DEFAULT_SORT,
  ADMIN_CLIENTS_PAGE_SIZE,
} from "@/features/admin/helpers/admin-clients.constants";
import type {
  AdminCategories,
  AdminCategoriesQuery,
  AdminClient,
  AdminClientDetail,
  AdminClientSegment,
  AdminClients,
  AdminClientsQuery,
  AdminClientStatus,
  AdminPaymentStatus,
  AdminPaymentStatusFilter,
  AdminProductsQuery,
  AdminNavigationCounts,
  AdminOrders,
  AdminOrdersQuery,
  AdminPayments,
  AdminPaymentsQuery,
  AdminProducts,
  AdminSummary,
} from "@/features/admin/types";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

export {
  ADMIN_CLIENTS_DEFAULT_SORT,
  ADMIN_CLIENTS_PAGE_SIZE,
} from "@/features/admin/helpers/admin-clients.constants";

const LOW_STOCK_THRESHOLD = 5;
const LOW_STOCK_SIZE = 5;

/** The wireframe draws eight rows, newest first. */
export const ADMIN_PRODUCTS_PAGE_SIZE = 8;
export const ADMIN_PRODUCTS_DEFAULT_SORT = "id,desc";

/** Admin orders wireframe: 8 rows, newest first. */
export const ADMIN_ORDERS_PAGE_SIZE = 8;
export const ADMIN_ORDERS_DEFAULT_SORT = "createdAt,desc";

/** The categories wireframe draws six rows, ordered A–Z. */
export const ADMIN_CATEGORIES_PAGE_SIZE = 6;
export const ADMIN_CATEGORIES_DEFAULT_SORT = "name,asc";

/** The payments wireframe draws eight rows, newest first, in a 30-day range. */
export const ADMIN_PAYMENTS_PAGE_SIZE = 8;
export const ADMIN_PAYMENTS_DEFAULT_SORT = "paidAt,desc";
export const ADMIN_PAYMENTS_RANGE_DAYS = 30;

const ADMIN_PAYMENT_API_STATUS: Record<AdminPaymentStatus, string> = {
  aprobado: "APPROVED",
  pendiente: "PENDING",
  rechazado: "REJECTED",
};

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

type Page<T> = { content: T[]; totalElements?: number };

/** Backend `CategoryDto` as served by `GET /api/categories`. */
type CategoryDto = {
  categoryId?: string | number;
  id?: string | number;
  name: string;
  slug: string;
  description?: string | null;
  featured?: boolean;
  active?: boolean;
  imageUrl?: string | null;
  productCount?: number;
  lowStockCount?: number;
};

/** The real API serves Spring `Page`; mock mode still serves plain arrays. */
function unwrapPage<T>(payload: Page<T> | T[]): T[] {
  return Array.isArray(payload) ? payload : payload.content;
}

/**
 * Categories list and the row the detail card opens.
 *
 * The public catalog endpoint backs both this screen and the product form, so
 * the same category ids drive the admin rows and the store links. This screen
 * has no mock route: the Bearer token and the 0-based `Pageable` conversion
 * live here, while callers keep their 1-based page numbers.
 */
export async function getAdminCategories(
  query: AdminCategoriesQuery = {},
): Promise<AdminCategories> {
  const headers = await adminAuthHeaders();
  const uiPage = query.page ?? 1;
  const params: Record<string, string | number | boolean> = {
    page: Math.max(0, uiPage - 1),
    sort: query.sort ?? ADMIN_CATEGORIES_DEFAULT_SORT,
  };
  if (query.q) params.q = query.q;
  if (query.featured !== undefined) params.featured = query.featured;
  if (query.size !== undefined) params.size = query.size;

  const { data } = await httpClient.get<Page<CategoryDto> | CategoryDto[]>(
    "/api/categories",
    { params, headers },
  );
  const items = unwrapPage(data).map((dto) => ({
    id: String(dto.categoryId ?? dto.id),
    name: dto.name,
    slug: dto.slug,
    description: dto.description ?? "",
    productCount: Number(dto.productCount ?? 0),
    lowStockCount: Number(dto.lowStockCount ?? 0),
    featured: Boolean(dto.featured),
    active: dto.active ?? true,
    imageUrl: dto.imageUrl ?? null,
  }));

  return {
    items,
    selectedId: items[0]?.id ?? "",
    updatedLabel: "hoy",
    total: Array.isArray(data) ? data.length : (data.totalElements ?? items.length),
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

type PaymentLookup = { provider?: string; paymentMethodId?: string; paymentType?: string };

async function resolveOrderPaymentMethods(
  orderIds: number[],
  headers: Record<string, string>,
): Promise<Map<number, string>> {
  const entries = await Promise.allSettled(
    orderIds.map(async (orderId) => {
      const { data } = await httpClient.get<PaymentLookup>(`/api/payments/order/${orderId}`, {
        headers,
      });
      const label = data.provider || data.paymentMethodId || data.paymentType || "";
      return [orderId, label] as const;
    }),
  );

  const map = new Map<number, string>();
  for (const entry of entries) {
    if (entry.status === "fulfilled") {
      map.set(entry.value[0], entry.value[1]);
    }
  }
  return map;
}

/** Orders table: visible page, store totals and the status tab counts. */
export async function getAdminOrders(
  query: AdminOrdersQuery = {},
): Promise<AdminOrders> {
  const headers = await adminAuthHeaders();
  const size = query.size ?? ADMIN_ORDERS_PAGE_SIZE;
  const uiPage = query.page ?? 1;
  const params: Record<string, string | number> = {
    page: Math.max(0, uiPage - 1),
    size,
    sort: query.sort ?? ADMIN_ORDERS_DEFAULT_SORT,
  };
  if (query.q) params.q = query.q;
  if (query.status && query.status !== "all") {
    const statusMap: Record<string, string> = {
      pendiente: "PENDING",
      pagado: "PAID",
      enviado: "SHIPPED",
      entregado: "DELIVERED",
      cancelado: "CANCELLED",
    };
    params.status = statusMap[query.status] ?? query.status.toUpperCase();
  }

  const [list, stats] = await Promise.all([
    httpClient.get<{ content?: AdminOrderApi[] } | AdminOrderApi[]>("/api/orders/admin", {
      params,
      headers,
    }),
    httpClient.get<AdminOrderStatsApi>("/api/orders/admin/stats", { headers }),
  ]);

  const rawItems = Array.isArray(list.data) ? list.data : (list.data.content ?? []);
  const paymentMethods = await resolveOrderPaymentMethods(
    rawItems.map((item) => item.id),
    headers,
  );
  const items = rawItems.map((item) => ({
    ...item,
    paymentMethod: item.paymentMethod ?? paymentMethods.get(item.id) ?? null,
  }));

  return mapAdminOrders({ items, stats: stats.data, pageSize: size });
}

/**
 * Payments table: visible page, settlement totals and the period KPIs.
 *
 * This screen has no mock route. The UI page is 1-based while Spring is
 * 0-based, and the list and its extended summary share the same Bearer token.
 */
export async function getAdminPayments(
  query: AdminPaymentsQuery = {},
): Promise<AdminPayments> {
  const headers = await adminAuthHeaders();
  const size = query.size ?? ADMIN_PAYMENTS_PAGE_SIZE;
  const uiPage = query.page ?? 1;
  const params: Record<string, string | number> = {
    page: Math.max(0, uiPage - 1),
    size,
    sort: query.sort ?? ADMIN_PAYMENTS_DEFAULT_SORT,
    days: ADMIN_PAYMENTS_RANGE_DAYS,
  };
  if (query.q) {
    params.q = query.q;
  }
  if (query.status && query.status !== "all" && query.status !== "todos") {
    params.status = ADMIN_PAYMENT_API_STATUS[query.status];
  }

  const [list, summary] = await Promise.all([
    httpClient.get<Page<AdminPaymentApi> | AdminPaymentApi[]>("/api/payments/admin", {
      params,
      headers,
    }),
    httpClient.get<AdminPaymentsSummaryApi>("/api/payments/admin/summary", {
      params: { days: ADMIN_PAYMENTS_RANGE_DAYS },
      headers,
    }),
  ]);

  return {
    items: unwrapPage(list.data).map(mapAdminPayment),
    pageSize: size,
    ...mapAdminPaymentSummary(summary.data),
  };
}

/** Backend `AdminCustomerResponse` served by `GET /api/customers/admin`. */
type AdminCustomerApi = {
  customerId?: string | number;
  id?: string | number;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string | null;
  status?: string;
  createdAt?: string;
  lastLoginAt?: string | null;
  orderCount?: number;
  totalSpent?: number | string;
  lastPurchaseAt?: string | null;
  segment?: string;
};

/** Backend `AdminCustomerSummaryResponse`; all figures are already aggregated. */
type AdminCustomerSummaryApi = {
  totalCustomers?: number;
  newThisMonth?: number;
  recurring?: number;
  averageTicket?: number;
};

/** Existing `CustomerDto` reused for the detail dialog. */
type CustomerDetailApi = {
  id?: string | number;
  customerId?: string | number;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string | null;
  status?: string;
  newsletterOptIn?: boolean;
  newsletterConsentAt?: string | null;
  createdAt?: string;
  lastLoginAt?: string | null;
  updatedAt?: string;
};

function asNumber(value: unknown): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function toClientSegment(value: unknown): AdminClientSegment | undefined {
  if (
    value === "nueva" ||
    value === "recurrente" ||
    value === "activa" ||
    value === "inactiva" ||
    value === "sin-compras"
  ) {
    return value;
  }
  return undefined;
}

/**
 * The wireframe badge follows the backend segment; `ACTIVE`/`SUSPENDED` is the
 * account status and is only used for the detail where no segment is present.
 * `sin-compras` keeps its own value so the filter stays distinct from the
 * `inactiva` badge printed for suspended accounts.
 */
function statusFromSegment(segment?: AdminClientSegment): AdminClientStatus {
  return segment ?? "activa";
}

function statusFromBackend(value?: string): AdminClientStatus {
  return value === "SUSPENDED" ? "inactiva" : "activa";
}

function initialsFor(firstName?: string, lastName?: string, fallback?: string) {
  const initials = `${firstName?.trim().charAt(0) ?? ""}${lastName?.trim().charAt(0) ?? ""}`.toUpperCase();
  return initials || fallback?.slice(0, 2).toUpperCase() || "--";
}

function formatLastPurchase(value?: string | null): string {
  if (!value) {
    return "—";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  const pad = (part: number) => String(part).padStart(2, "0");
  return `${pad(date.getUTCDate())}/${pad(date.getUTCMonth() + 1)} · ${pad(date.getUTCHours())}:${pad(date.getUTCMinutes())}`;
}

function mapAdminCustomer(dto: AdminCustomerApi): AdminClient {
  const segment = toClientSegment(dto.segment);
  const firstName = dto.firstName ?? "";
  const lastName = dto.lastName ?? "";

  return {
    customerId: String(dto.customerId ?? dto.id ?? ""),
    firstName,
    lastName,
    name: `${firstName} ${lastName}`.trim(),
    initials: initialsFor(firstName, lastName, dto.email),
    email: dto.email ?? "",
    phone: dto.phone ?? "",
    orderCount: asNumber(dto.orderCount),
    totalSpent: asNumber(dto.totalSpent),
    lastPurchase: formatLastPurchase(dto.lastPurchaseAt),
    ...(dto.lastPurchaseAt ? { lastPurchaseAt: dto.lastPurchaseAt } : {}),
    ...(dto.createdAt ? { createdAt: dto.createdAt } : {}),
    ...(dto.lastLoginAt ? { lastLoginAt: dto.lastLoginAt } : {}),
    ...(segment ? { segment } : {}),
    status: statusFromSegment(segment),
  };
}

function clientFilterParams(query: AdminClientsQuery): Record<string, string> {
  const params: Record<string, string> = {};
  if (query.name) params.name = query.name;
  if (query.email) params.email = query.email;
  if (query.phone) params.phone = query.phone;
  if (query.status) params.status = query.status;
  if (query.segment) params.segment = query.segment;
  return params;
}

/**
 * Clients table: real page, summary KPIs and a Bearer token on every call.
 *
 * This screen has no mock branch: even when `NEXT_PUBLIC_USE_MOCKS` is set, the
 * wireframe route and seed are gone and the gateway contract is the only source.
 */
export async function getAdminClients(
  query: AdminClientsQuery = {},
): Promise<AdminClients> {
  const headers = await adminAuthHeaders();
  const size = query.size ?? ADMIN_CLIENTS_PAGE_SIZE;
  const uiPage = query.page ?? 1;
  const filterParams = clientFilterParams(query);
  const params: Record<string, string | number> = {
    page: Math.max(0, uiPage - 1),
    size,
    sort: query.sort ?? ADMIN_CLIENTS_DEFAULT_SORT,
    ...filterParams,
  };

  const [list, summary] = await Promise.all([
    httpClient.get<Page<AdminCustomerApi> | AdminCustomerApi[]>(
      "/api/customers/admin",
      { params, headers },
    ),
    httpClient.get<AdminCustomerSummaryApi>("/api/customers/admin/summary", {
      params: filterParams,
      headers,
    }),
  ]);

  return {
    items: unwrapPage(list.data).map(mapAdminCustomer),
    pageSize: size,
    totalClients: asNumber(summary.data.totalCustomers),
    newThisMonth: asNumber(summary.data.newThisMonth),
    recurring: asNumber(summary.data.recurring),
    averageTicket: asNumber(summary.data.averageTicket),
  };
}

/**
 * Reuses the existing customer detail endpoint. The list row carries the order
 * aggregates, so opening a row never needs a parallel admin stats endpoint.
 */
export async function getAdminClientDetail(
  customerId: string,
  row?: AdminClient,
): Promise<AdminClientDetail> {
  const headers = await adminAuthHeaders();
  const { data } = await httpClient.get<CustomerDetailApi>(
    `/api/customers/${encodeURIComponent(customerId)}`,
    { headers },
  );
  const id = String(data.customerId ?? data.id ?? customerId);
  const firstName = data.firstName ?? row?.firstName ?? "";
  const lastName = data.lastName ?? row?.lastName ?? "";
  const name = `${firstName} ${lastName}`.trim();
  const base: AdminClient = row ?? {
    customerId: id,
    firstName,
    lastName,
    name,
    initials: initialsFor(firstName, lastName, data.email),
    email: data.email ?? "",
    phone: data.phone ?? "",
    orderCount: 0,
    totalSpent: 0,
    lastPurchase: formatLastPurchase(null),
    ...(data.createdAt ? { createdAt: data.createdAt } : {}),
    ...(data.lastLoginAt ? { lastLoginAt: data.lastLoginAt } : {}),
    status: statusFromBackend(data.status),
  };
  const initials = initialsFor(firstName, lastName, data.email ?? base.email);

  return {
    ...base,
    customerId: id,
    firstName,
    lastName,
    name: name || base.name,
    initials: initials === "--" ? base.initials : initials,
    email: data.email ?? base.email,
    phone: data.phone ?? base.phone,
    ...(data.createdAt ? { createdAt: data.createdAt } : {}),
    ...(data.lastLoginAt !== undefined
      ? { lastLoginAt: data.lastLoginAt }
      : {}),
    status: statusFromBackend(data.status),
    ...(data.newsletterOptIn !== undefined
      ? { newsletterOptIn: data.newsletterOptIn }
      : {}),
    ...(data.newsletterConsentAt !== undefined
      ? { newsletterConsentAt: data.newsletterConsentAt }
      : {}),
    ...(data.updatedAt ? { updatedAt: data.updatedAt } : {}),
  };
}
