/** Order states the dashboard renders as pills. */
export type AdminOrderStatus =
  | "pendiente"
  | "pagado"
  | "enviado"
  | "entregado"
  | "cancelado";

/** One column of the seven-day sales chart. */
export type AdminSalesDay = {
  /** Weekday short label drawn under the bar, e.g. `Lun`. */
  label: string;
  amount: number;
  /** Marks the day drawn inverse: the current one. */
  current?: boolean;
};

/** Row of the "Últimos pedidos" table, restricted to the rendered fields. */
export type AdminRecentOrder = {
  /** Display id, e.g. `#1042`. */
  id: string;
  customer: string;
  /** Display date, e.g. `10/11 · 09:42`. */
  placedAt: string;
  status: AdminOrderStatus;
  total: number;
};

/** Row of the "Stock bajo" card. */
export type AdminLowStockProduct = {
  name: string;
  sku: string;
  quantity: number;
};

/** Row of the "Actividad reciente" card. */
export type AdminActivityEntry = {
  id: string;
  text: string;
  /** Relative display time, e.g. `Hace 12 min`. */
  when: string;
};

/**
 * `GET /admin/summary` seed.
 *
 * Dates and relative times are display strings because the mock has no clock;
 * a real server formats them the same way.
 */
export type AdminSummary = {
  /** e.g. `Martes 10 de noviembre de 2026`. */
  dateLabel: string;
  /** e.g. `Actualizado hace 2 min`. */
  updatedLabel: string;
  salesToday: number;
  ordersToday: number;
  /** Percent change of today's sales vs. yesterday. */
  salesDelta: number;
  pendingOrders: number;
  pendingWithoutPayment: number;
  paymentsToClear: number;
  paymentsInReview: number;
  /** Units that still count as healthy stock. */
  lowStockThreshold: number;
  /** Percent change of the week vs. the previous one. */
  weekDelta: number;
  weekOrders: number;
  salesByDay: AdminSalesDay[];
  recentOrders: AdminRecentOrder[];
  lowStock: AdminLowStockProduct[];
  activity: AdminActivityEntry[];
};
