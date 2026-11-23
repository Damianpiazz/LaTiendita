"use client";

import { downloadCsv } from "@/features/admin/helpers/download-csv";
import type { AdminPayments } from "@/features/admin/types";

/**
 * Page header of the payments screen. CSV export uses the loaded rows; there
 * is no reconciliation endpoint in the frozen gateway contract yet, so that
 * control stays visibly disabled instead of pretending to do something.
 */
export function AdminPaymentsHeader({ payments }: { payments: AdminPayments }) {
  function exportCsv() {
    downloadCsv("pagos.csv", payments.items.map((payment) => ({
      Pago: payment.id,
      Pedido: payment.orderId,
      Cliente: payment.customer,
      Email: payment.email ?? "",
      Proveedor: payment.provider,
      Metodo: payment.method,
      Fecha: payment.paidAt,
      Monto: payment.amount,
      Estado: payment.status,
    })));
  }

  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Pagos</h1>
        <p className="text-sm text-wf-ink-muted">
          {payments.totalPayments} pagos · {payments.approvedCount} acreditados ·{" "}
          {payments.rejectedCount} rechazados por revisar
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={exportCsv}
          disabled={payments.items.length === 0}
          title={payments.items.length === 0 ? "No hay pagos para exportar" : undefined}
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:opacity-60"
        >
          Exportar CSV
        </button>
        <button
          type="button"
          disabled
          title="La conciliación automática todavía no está disponible"
          className="flex h-10 cursor-not-allowed items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-muted"
        >
          Conciliar pagos
        </button>
      </div>
    </header>
  );
}
