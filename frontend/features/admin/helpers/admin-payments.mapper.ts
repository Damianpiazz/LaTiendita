import type { AdminPayment } from "@/features/admin/types";

/**
 * Wire shape of `GET /api/payments/admin`. Amounts and some summary counters
 * arrive as strings from Java `BigDecimal`, so both shapes are accepted.
 */
export type AdminPaymentApi = {
  id: number;
  orderNumber?: string | null;
  customerId: string;
  provider?: string | null;
  paymentMethodId?: string | null;
  paymentType?: string | null;
  paidAt?: string | null;
  amount: string | number;
  status?: string | null;
  /** Optional only when the gateway exposes it; never synthesized. */
  email?: string | null;
};

/** Wire shape of `GET /api/payments/admin/summary?days=30`. */
export type AdminPaymentSummaryApi = {
  paymentsToClear: string | number;
  paymentsInReview: string | number;
  totalPayments: string | number;
  approvedCount: string | number;
  pendingCount: string | number;
  rejectedCount: string | number;
  approvedVolume: string | number;
  pendingVolume: string | number;
  rejectedVolume: string | number;
  approvalRate: string | number;
  approvalAttempts: string | number;
  rangeDays: string | number;
};

const PROVIDER_LABELS: Record<string, string> = {
  MERCADO_PAGO: "Mercado Pago",
};

const STATUS_LABELS: Record<string, AdminPayment["status"]> = {
  APPROVED: "aprobado",
  REJECTED: "rechazado",
};

const METHOD_LABELS: Record<string, string> = {
  credit_card: "Tarjeta de crédito",
  debit_card: "Tarjeta de débito",
  account_money: "Dinero en cuenta",
  bank_transfer: "Transferencia",
  ticket: "Efectivo",
  cash: "Efectivo",
};

const CARD_TYPES = new Set(["credit_card", "debit_card"]);

const CARD_BRANDS: Record<string, string> = {
  visa: "Visa",
  mastercard: "Mastercard",
  master: "Mastercard",
  master_card: "Mastercard",
  amex: "American Express",
  american_express: "American Express",
};

const paidAtFormatter = new Intl.DateTimeFormat("es-AR", {
  timeZone: "America/Argentina/Buenos_Aires",
  day: "2-digit",
  month: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hourCycle: "h23",
});

function asNumber(value: string | number): number {
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function orderLabel(orderNumber: string | null | undefined): string {
  const value = orderNumber?.trim();
  if (!value) {
    return "";
  }
  if (value.startsWith("#")) {
    return value;
  }
  return `#${value.startsWith("ORD-") ? value.slice(4) : value}`;
}

function methodLabel(dto: AdminPaymentApi): string {
  const paymentType = dto.paymentType?.trim().toLowerCase() ?? "";
  const methodId = dto.paymentMethodId?.trim().toLowerCase() ?? "";
  const label = METHOD_LABELS[paymentType] ?? METHOD_LABELS[methodId] ?? "";

  if (!label) {
    return "";
  }

  const brand = CARD_TYPES.has(paymentType) ? CARD_BRANDS[methodId] : undefined;
  return brand ? `${label} · ${brand}` : label;
}

function formatPaidAt(value: string | null | undefined): string {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const parts = Object.fromEntries(
    paidAtFormatter.formatToParts(date).map((part) => [part.type, part.value]),
  );

  return `${parts.day}/${parts.month} · ${parts.hour}:${parts.minute}`;
}

/** Backend row to the wireframe row: no customer name or email is invented. */
export function mapAdminPayment(dto: AdminPaymentApi): AdminPayment {
  const provider = dto.provider?.trim() ?? "";

  return {
    id: `#${dto.id}`,
    orderId: orderLabel(dto.orderNumber),
    customer: dto.customerId,
    ...(dto.email ? { email: dto.email } : {}),
    provider: PROVIDER_LABELS[provider] ?? provider,
    method: methodLabel(dto),
    paidAt: formatPaidAt(dto.paidAt),
    amount: asNumber(dto.amount),
    status: dto.status ? (STATUS_LABELS[dto.status] ?? "pendiente") : "pendiente",
  };
}

/** Admin summary counters to the header, KPI and tab figures of the screen. */
export function mapAdminPaymentSummary(api: AdminPaymentSummaryApi) {
  return {
    totalPayments: asNumber(api.totalPayments),
    approvedCount: asNumber(api.approvedCount),
    pendingCount: asNumber(api.pendingCount),
    rejectedCount: asNumber(api.rejectedCount),
    approvedVolume: asNumber(api.approvedVolume),
    pendingVolume: asNumber(api.pendingVolume),
    rejectedVolume: asNumber(api.rejectedVolume),
    approvalRate: asNumber(api.approvalRate),
    approvalAttempts: asNumber(api.approvalAttempts),
    rangeLabel: `Últimos ${asNumber(api.rangeDays)} días`,
  };
}
