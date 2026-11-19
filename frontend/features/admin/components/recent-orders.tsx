import Link from "next/link";

import { StatusBadge } from "@/features/admin/components/status-badge";
import type { AdminSummary } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Latest orders card: header plus a six-row table.
 *
 * The sub line and the `12` badge are the store totals, not the six visible
 * rows. Rows reproduce the frame's fixed column widths and scroll sideways on
 * narrow screens.
 */
export function RecentOrders({ summary }: { summary: AdminSummary }) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="flex flex-wrap items-center justify-between gap-4 p-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">Últimos pedidos</h2>
          <p className="text-xs text-wf-ink-muted">
            {summary.pendingOrders} pendientes de despacho ·{" "}
            {summary.paymentsInReview} pagos en revisión
          </p>
        </div>
        <Link
          href="/admin/pedidos"
          className="text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          Ver todos →
        </Link>
      </div>

      <div className="overflow-x-auto">
        <div role="table" aria-label="Últimos pedidos" className="min-w-[720px]">
          <div
            role="row"
            className="flex h-10 items-center border-y border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted"
          >
            <div role="columnheader" className="w-[110px]">
              Pedido
            </div>
            <div role="columnheader" className="w-[200px]">
              Cliente
            </div>
            <div role="columnheader" className="w-[120px]">
              Fecha
            </div>
            <div role="columnheader" className="w-[130px]">
              Estado
            </div>
            <div role="columnheader" className="w-[110px]">
              Total
            </div>
            <div
              role="columnheader"
              className="flex-1 text-right"
            >
              Acción
            </div>
          </div>

          {summary.recentOrders.map((order) => (
            <div
              key={order.id}
              role="row"
              className="flex h-14 items-center border-b border-wf-line px-5 text-sm"
            >
              <div role="cell" className="w-[110px] font-medium text-wf-ink">
                {order.id}
              </div>
              <div role="cell" className="w-[200px] text-wf-ink-secondary">
                {order.customer}
              </div>
              <div role="cell" className="w-[120px] text-wf-ink-muted">
                {order.placedAt}
              </div>
              <div role="cell" className="w-[130px]">
                <StatusBadge status={order.status} />
              </div>
              <div role="cell" className="w-[110px] font-medium text-wf-ink">
                {formatPrice(order.total)}
              </div>
              <div role="cell" className="flex-1 text-right">
                <Link
                  href={`/admin/pedidos/${order.id.replace("#", "")}`}
                  className="text-wf-ink-secondary transition-colors hover:text-wf-ink"
                >
                  Ver →
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
