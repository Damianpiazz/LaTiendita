import type { AdminOrderStatus } from "./dashboard";

/** Payment method printed in the orders table. */
export type AdminPaymentMethod =
  | "mercado-pago"
  | "transferencia"
  | "efectivo";

/** One row of the admin orders table. */
export type AdminOrder = {
  /** Display id, e.g. `#1042`. */
  id: string;
  customer: string;
  email: string;
  /** Display date, e.g. `10/11 · 09:42`. */
  placedAt: string;
  itemCount: number;
  payment: AdminPaymentMethod;
  total: number;
  status: AdminOrderStatus;
};

/** `GET /admin/orders` seed. */
export type AdminOrders = {
  items: AdminOrder[];
  /** Table size, e.g. 8, and store totals, e.g. 48. */
  pageSize: number;
  totalOrders: number;
  pendingDispatch: number;
  cancelledInPeriod: number;
  /** Counts behind the status tabs; `todos` is the store total. */
  statusCounts: Record<"todos" | AdminOrderStatus, number>;
  /** e.g. `Últimos 30 días`; display string because the mock has no clock. */
  rangeLabel: string;
};
