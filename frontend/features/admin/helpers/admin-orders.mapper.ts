import type {
  AdminOrder,
  AdminOrders,
  AdminPaymentMethod,
} from "@/features/admin/types";
import { formatAdminDateLabel, mapOrderStatus } from "./admin-summary.mapper";

export type AdminOrderApi = {
  id: number;
  orderNumber: string;
  customerId: string;
  customerEmail?: string | null;
  paymentMethod?: string | null;
  status: string;
  total: number | string;
  itemCount: number;
  createdAt: string;
};

export type AdminOrderStatsApi = {
  totalOrders: number;
  pendingDispatch: number;
  cancelledInPeriod: number;
  statusCounts: {
    todos: number;
    pendiente: number;
    pagado: number;
    enviado: number;
    entregado: number;
    cancelado: number;
  };
};

function toNumber(value: number | string): number {
  if (typeof value === "number") return value;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function shortOrderId(orderNumber: string, id: number): string {
  const trimmed = orderNumber.replace(/^ORD-?/i, "");
  return `#${trimmed || id}`;
}

export function mapPaymentMethod(value?: string | null): AdminPaymentMethod {
  const normalized = (value ?? "").toLowerCase().trim();
  if (!normalized) return "—";
  if (normalized.includes("transfer")) return "transferencia";
  if (normalized.includes("efectivo") || normalized.includes("cash")) return "efectivo";
  if (normalized.includes("mercado") || normalized.includes("mp")) return "mercado-pago";
  return "—";
}

export function mapAdminOrder(api: AdminOrderApi): AdminOrder {
  return {
    id: shortOrderId(api.orderNumber, api.id),
    customer: api.customerId || "Cliente",
    email: api.customerEmail?.trim() || api.customerId || "—",
    placedAt: formatAdminDateLabel(new Date(api.createdAt)),
    itemCount: api.itemCount,
    payment: mapPaymentMethod(api.paymentMethod),
    total: toNumber(api.total),
    status: mapOrderStatus(api.status),
  };
}

export function mapAdminOrders(args: {
  items: AdminOrderApi[];
  stats: AdminOrderStatsApi;
  pageSize: number;
}): AdminOrders {
  const statusCounts = args.stats.statusCounts;
  return {
    items: args.items.map(mapAdminOrder),
    pageSize: args.pageSize,
    totalOrders: args.stats.totalOrders,
    pendingDispatch: args.stats.pendingDispatch,
    cancelledInPeriod: args.stats.cancelledInPeriod,
    statusCounts: {
      todos: statusCounts.todos,
      pendiente: statusCounts.pendiente,
      pagado: statusCounts.pagado,
      enviado: statusCounts.enviado,
      entregado: statusCounts.entregado,
      cancelado: statusCounts.cancelado,
    },
    rangeLabel: "Últimos 30 días",
  };
}