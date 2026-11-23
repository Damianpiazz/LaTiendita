import type { AdminPayment, AdminPayments } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

import { PaymentStatusBadge } from "./payment-status-badge";

const SORT_OPTIONS = [
  { value: "paidAt,desc", label: "Más recientes ↓" },
  { value: "paidAt,asc", label: "Más antiguos ↑" },
  { value: "amount,desc", label: "Mayor monto ↓" },
  { value: "amount,asc", label: "Menor monto ↑" },
];

type AdminPaymentsTableProps = {
  payments: AdminPayments;
  search: string;
  sort: string;
  onSearchChange: (value: string) => void;
  onSortChange: (value: string) => void;
  onSelect: (payment: AdminPayment) => void;
};

/**
 * Payments card: header with the live search and sort, the 40px head and the
 * mapped rows. Column widths are the frame's; the wrapper scrolls sideways
 * under 1096px and the status pill stays payments-specific.
 */
export function AdminPaymentsTable({
  payments,
  search,
  sort,
  onSearchChange,
  onSortChange,
  onSelect,
}: AdminPaymentsTableProps) {
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
          <form
            role="search"
            className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[280px]"
            onSubmit={(event) => event.preventDefault()}
          >
            <label htmlFor="admin-payments-search" className="sr-only">
              Buscar pagos
            </label>
            <input
              id="admin-payments-search"
              type="search"
              value={search}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder="Buscar por pago, pedido o cliente..."
              className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
            />
          </form>
          <label
            htmlFor="admin-payments-sort"
            className="text-sm font-medium text-wf-ink-secondary"
          >
            Ordenar:
          </label>
          <select
            id="admin-payments-sort"
            aria-label="Ordenar"
            value={sort}
            onChange={(event) => onSortChange(event.target.value)}
            className="bg-transparent text-sm font-medium text-wf-ink-secondary outline-none"
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
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
                {payment.email ? (
                  <span className="truncate text-xs text-wf-ink-muted">
                    {payment.email}
                  </span>
                ) : null}
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
                <button
                  type="button"
                  onClick={() => onSelect(payment)}
                  aria-label={`Ver detalle del pago ${payment.id}`}
                  className="underline-offset-2 hover:text-wf-ink hover:underline"
                >
                  Ver →
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
