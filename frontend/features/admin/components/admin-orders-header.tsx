"use client";

import { downloadCsv } from "@/features/admin/helpers/download-csv";
import { printOrderLabels } from "@/features/admin/helpers/print-labels";
import type { AdminOrders, AdminPaymentMethod } from "@/features/admin/types";

const paymentLabels: Record<AdminPaymentMethod, string> = {
  "mercado-pago": "Mercado Pago",
  transferencia: "Transferencia",
  efectivo: "Efectivo",
  "—": "—",
};

/**
 * Page header of the orders screen: title, store totals and the two actions.
 * Both work with the rows already loaded by the table, so no extra endpoint is
 * needed for exporting or printing.
 */
export function AdminOrdersHeader({ orders }: { orders: AdminOrders }) {
  const empty = orders.items.length === 0;

  function exportCsv() {
    downloadCsv("pedidos.csv", orders.items.map((order) => ({
      Pedido: order.id,
      Cliente: order.customer,
      Email: order.email,
      Fecha: order.placedAt,
      Articulos: order.itemCount,
      Pago: paymentLabels[order.payment],
      Total: order.total,
      Estado: order.status,
    })));
  }

  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Pedidos</h1>
        <p className="text-sm text-wf-ink-muted">
          {orders.totalOrders} pedidos · {orders.pendingDispatch} pendientes de
          despacho
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={exportCsv}
          disabled={empty}
          title={empty ? "No hay pedidos para exportar" : undefined}
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:opacity-60"
        >
          Exportar CSV
        </button>
        <button
          type="button"
          onClick={() => printOrderLabels(orders.items)}
          disabled={empty}
          title={empty ? "No hay etiquetas para imprimir" : undefined}
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-muted transition-colors hover:bg-wf-canvas hover:text-wf-ink disabled:cursor-not-allowed disabled:opacity-60"
        >
          Imprimir etiquetas
        </button>
      </div>
    </header>
  );
}
