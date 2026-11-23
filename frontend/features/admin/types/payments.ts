/** Settlement state printed in the payments table. */
export type AdminPaymentStatus = "aprobado" | "pendiente" | "rechazado";

/** Status pill selection; `all` and `todos` send no `status` query param. */
export type AdminPaymentStatusFilter = "all" | "todos" | AdminPaymentStatus;

/** One row of the admin payments table. */
export type AdminPayment = {
  /** Display id, e.g. `#812`. */
  id: string;
  /** Order the payment belongs to, e.g. `#1042`. */
  orderId: string;
  customer: string;
  /** The gateway does not persist email; the line is omitted when absent. */
  email?: string;
  /** Gateway that processed the payment, e.g. `Mercado Pago`. */
  provider: string;
  /** Instrument used inside the gateway, e.g. `Tarjeta de crédito`. */
  method: string;
  /** Display date, e.g. `10/11 · 09:42`. */
  paidAt: string;
  amount: number;
  status: AdminPaymentStatus;
};

/** `GET /api/payments/admin` page plus its extended summary. */
export type AdminPayments = {
  items: AdminPayment[];
  /** Table size, e.g. 8, and store totals, e.g. 48. */
  pageSize: number;
  totalPayments: number;
  approvedCount: number;
  pendingCount: number;
  rejectedCount: number;
  /** Money already settled, still clearing and rejected in the period. */
  approvedVolume: number;
  pendingVolume: number;
  rejectedVolume: number;
  /**
   * Approval rate in whole percent, e.g. 91. Seeded instead of derived: the
   * frame's `34 de 37 intentos` counts attempts, not settled payments.
   */
  approvalRate: number;
  approvalAttempts: number;
  /** e.g. `Últimos 30 días`; derived from the summary's `rangeDays`. */
  rangeLabel: string;
};

/** Public query of the payments screen; page is 1-based for the UI. */
export type AdminPaymentsQuery = {
  q?: string;
  status?: AdminPaymentStatusFilter;
  page?: number;
  size?: number;
  sort?: string;
  /** Days forwarded to both the payments list and its summary. */
  days?: number;
};
