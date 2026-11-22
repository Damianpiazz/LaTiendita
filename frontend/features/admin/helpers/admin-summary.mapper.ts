import type {
  AdminActivityEntry,
  AdminLowStockProduct,
  AdminOrderStatus,
  AdminRecentOrder,
  AdminSalesDay,
  AdminSummary,
} from "@/features/admin/types";

export type AdminOrderSummaryApi = {
  salesToday: number | string;
  salesYesterday: number | string;
  ordersToday: number;
  pendingOrders: number;
  pendingWithoutPayment: number;
  weekOrders: number;
  previousWeekSales: number | string;
  salesByDay: Array<{
    date: string;
    total: number | string;
    orders: number;
    averageTicket: number | string;
  }>;
  recentOrders: Array<{
    id: number;
    orderNumber: string;
    customerId: string;
    status: string;
    total: number | string;
    itemCount: number;
    createdAt: string;
  }>;
};

export type AdminPaymentSummaryApi = {
  paymentsToClear: number | string;
  paymentsInReview: number;
};

export type AdminLowStockApi = {
  items: Array<{ name: string; sku: string; quantity: number }>;
  totalLowStock: number;
  threshold: number;
};

const DAY_LABELS = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"] as const;
const AR_TIME_ZONE = "America/Argentina/Buenos_Aires";

function toNumber(value: number | string): number {
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function roundPercent(value: number): number {
  return Math.round(value * 10) / 10;
}

export function percentDelta(current: number, previous: number): number {
  if (previous <= 0) {
    return current > 0 ? 100 : 0;
  }
  return roundPercent(((current - previous) / previous) * 100);
}

export function formatAdminDateLabel(date: Date): string {
  const formatted = new Intl.DateTimeFormat("es-AR", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
    timeZone: AR_TIME_ZONE,
  }).format(date);
  return (formatted.charAt(0).toUpperCase() + formatted.slice(1)).replace(",", "");
}

export function formatPlacedAt(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return iso;
  const parts = new Intl.DateTimeFormat("es-AR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
    timeZone: AR_TIME_ZONE,
  }).formatToParts(date);
  const get = (type: Intl.DateTimeFormatPartTypes) =>
    parts.find((part) => part.type === type)?.value ?? "";
  return `${get("day")}/${get("month")} · ${get("hour")}:${get("minute")}`;
}

export function mapOrderStatus(status: string): AdminOrderStatus {
  const normalized = status.toLowerCase();
  if (normalized === "paid") return "pagado";
  if (normalized === "shipped") return "enviado";
  if (normalized === "delivered") return "entregado";
  if (normalized === "cancelled") return "cancelado";
  return "pendiente";
}

export function mapRecentOrder(
  order: AdminOrderSummaryApi["recentOrders"][number],
): AdminRecentOrder {
  return {
    id: `#${order.orderNumber.replace(/^ORD-?/i, "") || order.id}`,
    customer: order.customerId || "Cliente",
    placedAt: formatPlacedAt(order.createdAt),
    status: mapOrderStatus(order.status),
    total: toNumber(order.total),
  };
}

export function mapSalesByDay(
  days: AdminOrderSummaryApi["salesByDay"],
): AdminSalesDay[] {
  const lastIndex = days.length - 1;
  return days.map((day, index) => {
    const date = new Date(`${day.date}T00:00:00Z`);
    const weekday = Number.isNaN(date.getTime()) ? -1 : date.getUTCDay();
    return {
      label: weekday >= 0 ? DAY_LABELS[weekday] : day.date,
      amount: toNumber(day.total),
      ...(index === lastIndex ? { current: true } : {}),
    };
  });
}

function relativeTime(iso: string, now: Date): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return "recientemente";
  const diffMs = Math.max(0, now.getTime() - date.getTime());
  const minutes = Math.floor(diffMs / 60000);
  if (minutes < 1) return "Hace un momento";
  if (minutes < 60) return `Hace ${minutes} min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `Hace ${hours} h`;
  const days = Math.floor(hours / 24);
  return `Hace ${days} d`;
}

export function buildRecentActivity(
  recentOrders: AdminOrderSummaryApi["recentOrders"],
  lowStock: AdminLowStockProduct[],
  now: Date,
): AdminActivityEntry[] {
  const entries: AdminActivityEntry[] = recentOrders.slice(0, 3).map((order) => {
    const id = `#${order.orderNumber.replace(/^ORD-?/i, "") || order.id}`;
    const status = mapOrderStatus(order.status);
    const text =
      status === "pagado"
        ? `Pago acreditado ${id} · $ ${toNumber(order.total).toLocaleString("es-AR")}`
        : `Nuevo pedido ${id} de ${order.customerId || "cliente"} · $ ${toNumber(order.total).toLocaleString("es-AR")}`;
    return { id: `order-${order.id}`, text, time: relativeTime(order.createdAt, now) };
  });

  for (const product of lowStock.slice(0, Math.max(0, 5 - entries.length))) {
    entries.push({
      id: `stock-${product.sku}`,
      text: `Stock bajo ${product.name} · quedan ${product.quantity} u.`,
      time: "Revisar inventario",
    });
  }

  return entries.slice(0, 5);
}

export function mapAdminSummary(args: {
  orders: AdminOrderSummaryApi;
  payments: AdminPaymentSummaryApi;
  lowStock: AdminLowStockApi;
  now?: Date;
}): AdminSummary {
  const { orders, payments, lowStock } = args;
  const now = args.now ?? new Date();
  const salesToday = toNumber(orders.salesToday);
  const salesYesterday = toNumber(orders.salesYesterday);
  const weekSales = orders.salesByDay.reduce((sum, day) => sum + toNumber(day.total), 0);
  const previousWeekSales = toNumber(orders.previousWeekSales);

  return {
    dateLabel: formatAdminDateLabel(now),
    updatedLabel: "Actualizado ahora",
    salesToday,
    ordersToday: orders.ordersToday,
    salesDelta: percentDelta(salesToday, salesYesterday),
    pendingOrders: orders.pendingOrders,
    pendingWithoutPayment: orders.pendingWithoutPayment,
    paymentsToClear: toNumber(payments.paymentsToClear),
    paymentsInReview: payments.paymentsInReview,
    lowStockThreshold: lowStock.threshold,
    lowStockTotal: lowStock.totalLowStock,
    weekDelta: percentDelta(weekSales, previousWeekSales),
    weekOrders: orders.weekOrders,
    salesByDay: mapSalesByDay(orders.salesByDay),
    recentOrders: orders.recentOrders.map(mapRecentOrder),
    lowStockProducts: lowStock.items.map((item) => ({
      name: item.name,
      sku: item.sku,
      quantity: item.quantity,
    })),
    recentActivity: buildRecentActivity(orders.recentOrders, lowStock.items, now),
  };
}
