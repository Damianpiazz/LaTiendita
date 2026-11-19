import type { AdminOrders } from "@/features/admin/types";

/**
 * Page header of the orders screen: title, the two store totals and the
 * export/label actions.
 *
 * Neither action has a flow behind it in the frame, so both are presentational.
 */
export function AdminOrdersHeader({ orders }: { orders: AdminOrders }) {
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
        <span className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary">
          Exportar CSV
        </span>
        <span className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-muted">
          Imprimir etiquetas
        </span>
      </div>
    </header>
  );
}
