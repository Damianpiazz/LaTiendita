import { StatusBadge } from "@/features/admin/components/status-badge";
import type { AdminOrders, AdminPaymentMethod } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

const paymentLabels: Record<AdminPaymentMethod, string> = {
  "mercado-pago": "Mercado Pago",
  transferencia: "Transferencia",
  efectivo: "Efectivo",
};

/**
 * Orders card: header with search and sort, the 40px head and eight 64px rows.
 *
 * Column widths are the frame's; the wrapper scrolls sideways under 1096px.
 * The status pill reuses the five-state `StatusBadge` of the summary screen.
 */
export function AdminOrdersTable({ orders }: { orders: AdminOrders }) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="flex flex-wrap items-center justify-between gap-4 p-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">Todos los pedidos</h2>
          <p className="text-xs text-wf-ink-muted">
            {orders.totalOrders} pedidos · {orders.cancelledInPeriod} cancelados en
            el período
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-4">
          <div
            role="search"
            className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[280px]"
          >
            <label htmlFor="admin-orders-search" className="sr-only">
              Buscar pedidos
            </label>
            <input
              id="admin-orders-search"
              type="search"
              placeholder="Buscar por pedido, cliente o email..."
              className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
            />
          </div>
          <span className="text-sm font-medium text-wf-ink-secondary">
            Ordenar: Más recientes ↓
          </span>
        </div>
      </div>

      <div className="overflow-x-auto">
        <div role="table" aria-label="Pedidos" className="min-w-[1096px]">
          <div
            role="row"
            className="flex h-10 items-center border-y border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted"
          >
            <div role="columnheader" className="w-[110px]">
              Pedido
            </div>
            <div role="columnheader" className="w-[240px]">
              Cliente
            </div>
            <div role="columnheader" className="w-[130px]">
              Fecha
            </div>
            <div role="columnheader" className="w-[100px]">
              Artículos
            </div>
            <div role="columnheader" className="w-[130px]">
              Pago
            </div>
            <div role="columnheader" className="w-[110px]">
              Total
            </div>
            <div role="columnheader" className="w-[140px]">
              Estado
            </div>
            <div role="columnheader" className="flex-1 text-right">
              Acciones
            </div>
          </div>

          {orders.items.map((order) => (
            <div
              key={order.id}
              role="row"
              className="flex h-16 items-center border-b border-wf-line px-5 text-sm last:border-b-0"
            >
              <div role="cell" className="w-[110px] font-medium text-wf-ink">
                {order.id}
              </div>

              <div
                role="cell"
                className="flex w-[240px] min-w-0 flex-col gap-1"
              >
                <span className="truncate font-medium text-wf-ink">
                  {order.customer}
                </span>
                <span className="truncate text-xs text-wf-ink-muted">
                  {order.email}
                </span>
              </div>

              <div role="cell" className="w-[130px] text-wf-ink-muted">
                {order.placedAt}
              </div>
              <div role="cell" className="w-[100px] text-wf-ink-secondary">
                {order.itemCount} artículo{order.itemCount === 1 ? "" : "s"}
              </div>
              <div role="cell" className="w-[130px] text-wf-ink-secondary">
                {paymentLabels[order.payment]}
              </div>
              <div role="cell" className="w-[110px] font-medium text-wf-ink">
                {formatPrice(order.total)}
              </div>
              <div role="cell" className="w-[140px]">
                <StatusBadge status={order.status} />
              </div>
              <div
                role="cell"
                className="flex flex-1 justify-end font-medium text-wf-ink-secondary"
              >
                Ver detalle →
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
