import { PaymentStatusBadge } from "@/features/admin/components/payment-status-badge";
import type { AdminPayments } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Payments card: header with search and sort, the 40px head and eight 64px
 * rows.
 *
 * Column widths are the frame's; the wrapper scrolls sideways under 1096px.
 * The status pill is the payments-specific one, because the settlement
 * vocabulary differs from the orders'.
 */
export function AdminPaymentsTable({ payments }: { payments: AdminPayments }) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="flex flex-wrap items-center justify-between gap-4 p-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">Movimientos</h2>
          <p className="text-xs text-wf-ink-muted">
            Todos los pagos procesados por Mercado Pago
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-4">
          <div
            role="search"
            className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[280px]"
          >
            <label htmlFor="admin-payments-search" className="sr-only">
              Buscar pagos
            </label>
            <input
              id="admin-payments-search"
              type="search"
              placeholder="Buscar por pago, pedido o cliente..."
              className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
            />
          </div>
          <span className="text-sm font-medium text-wf-ink-secondary">
            Ordenar: Más recientes ↓
          </span>
        </div>
      </div>

      <div className="overflow-x-auto">
        <div role="table" aria-label="Pagos" className="min-w-[1096px]">
          <div
            role="row"
            className="flex h-10 items-center border-y border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted"
          >
            <div role="columnheader" className="w-[110px]">
              Pago
            </div>
            <div role="columnheader" className="w-[100px]">
              Pedido
            </div>
            <div role="columnheader" className="w-[210px]">
              Cliente
            </div>
            <div role="columnheader" className="w-[190px]">
              Método
            </div>
            <div role="columnheader" className="w-[130px]">
              Fecha
            </div>
            <div role="columnheader" className="w-[110px]">
              Monto
            </div>
            <div role="columnheader" className="w-[130px]">
              Estado
            </div>
            <div role="columnheader" className="flex-1 text-right">
              Acciones
            </div>
          </div>

          {payments.items.map((payment) => (
            <div
              key={payment.id}
              role="row"
              className="flex h-16 items-center border-b border-wf-line px-5 text-sm last:border-b-0"
            >
              <div role="cell" className="w-[110px] font-medium text-wf-ink">
                {payment.id}
              </div>
              <div role="cell" className="w-[100px] text-wf-ink-secondary">
                {payment.orderId}
              </div>

              <div role="cell" className="flex w-[210px] min-w-0 flex-col gap-1">
                <span className="truncate font-medium text-wf-ink">
                  {payment.customer}
                </span>
                <span className="truncate text-xs text-wf-ink-muted">
                  {payment.email}
                </span>
              </div>

              <div role="cell" className="flex w-[190px] min-w-0 flex-col gap-1">
                <span className="truncate text-wf-ink-secondary">
                  {payment.provider}
                </span>
                <span className="truncate text-xs text-wf-ink-muted">
                  {payment.method}
                </span>
              </div>

              <div role="cell" className="w-[130px] text-wf-ink-muted">
                {payment.paidAt}
              </div>
              <div role="cell" className="w-[110px] font-medium text-wf-ink">
                {formatPrice(payment.amount)}
              </div>
              <div role="cell" className="w-[130px]">
                <PaymentStatusBadge status={payment.status} />
              </div>
              <div
                role="cell"
                className="flex flex-1 justify-end font-medium text-wf-ink-secondary"
              >
                Ver →
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
