import { describe, expect, it } from "vitest";

import {
  mapAdminPayment,
  mapAdminPaymentSummary,
  type AdminPaymentApi,
  type AdminPaymentSummaryApi,
} from "./admin-payments.mapper";

const payment: AdminPaymentApi = {
  id: 812,
  orderNumber: "ORD-1042",
  customerId: "user-1",
  provider: "MERCADO_PAGO",
  paymentMethodId: "visa",
  paymentType: "credit_card",
  paidAt: "2026-11-10T12:42:00Z",
  amount: "45998.00",
  status: "PENDING",
};

const summary: AdminPaymentSummaryApi = {
  paymentsToClear: "86400.50",
  paymentsInReview: 11,
  totalPayments: 48,
  approvedCount: 34,
  pendingCount: 11,
  rejectedCount: 3,
  approvedVolume: "1284950.00",
  pendingVolume: 412370,
  rejectedVolume: 67994,
  approvalRate: 91,
  approvalAttempts: 37,
  rangeDays: 30,
};

describe("admin payments mapper", () => {
  it("maps a backend row to the wireframe row without inventing customer data", () => {
    expect(mapAdminPayment(payment)).toEqual({
      id: "#812",
      orderId: "#1042",
      customer: "user-1",
      provider: "Mercado Pago",
      method: "Tarjeta de crédito · Visa",
      paidAt: "10/11 · 09:42",
      amount: 45998,
      status: "pendiente",
    });
    expect(mapAdminPayment(payment)).not.toHaveProperty("email");
  });

  it("maps the three backend statuses and falls back to pending", () => {
    expect(mapAdminPayment({ ...payment, status: "APPROVED" }).status).toBe("aprobado");
    expect(mapAdminPayment({ ...payment, status: "REJECTED" }).status).toBe("rechazado");
    expect(mapAdminPayment({ ...payment, status: "CANCELLED" }).status).toBe("pendiente");
  });

  it("composes the method label from payment type and payment method id", () => {
    expect(
      mapAdminPayment({
        ...payment,
        paymentType: "account_money",
        paymentMethodId: "account_money",
      }).method,
    ).toBe("Dinero en cuenta");
    expect(
      mapAdminPayment({ ...payment, paymentType: null, paymentMethodId: "ticket" }).method,
    ).toBe("Efectivo");
    expect(mapAdminPayment({ ...payment, paymentType: null, paymentMethodId: null }).method).toBe(
      "",
    );
  });

  it("builds the header, KPI and tab figures from the extended summary", () => {
    expect(mapAdminPaymentSummary(summary)).toEqual({
      totalPayments: 48,
      approvedCount: 34,
      pendingCount: 11,
      rejectedCount: 3,
      approvedVolume: 1284950,
      pendingVolume: 412370,
      rejectedVolume: 67994,
      approvalRate: 91,
      approvalAttempts: 37,
      rangeLabel: "Últimos 30 días",
    });
    expect(mapAdminPaymentSummary({ ...summary, rangeDays: "7" }).rangeLabel).toBe(
      "Últimos 7 días",
    );
  });
});
