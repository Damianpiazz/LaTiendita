"use client";

import { StatusBadge } from "@/features/admin/components/status-badge";
import type {
  AdminOrder,
  AdminOrders,
  AdminPaymentMethod,
} from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

const paymentLabels: Record<AdminPaymentMethod, string> = {
  "mercado-pago": "Mercado Pago",
  transferencia: "Transferencia",
  efectivo: "Efectivo",
  "—": "—",
};

const SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Más recientes ↓" },
  { value: "createdAt,asc", label: "Más antiguos ↑" },
  { value: "total,desc", label: "Mayor total ↓" },
  { value: "total,asc", label: "Menor total ↑" },
];

type Props = {
  orders: AdminOrders;
  q: string;
  sort: string;
  loading: boolean;
  loadError: string | null;
  onSearchChange: (value: string) => void;
  onSortChange: (value: string) => void;
  onSelect: (order: AdminOrder) => void;
};

/** Orders card: header with live search and sort, 40px head and 64px rows. */
export function AdminOrdersTable({
  orders,
  q,
  sort,
  loading,
  loadError,
  onSearchChange,
  onSortChange,
  onSelect,
}: Props) {
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
              value={q}
              disabled={loading}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder="Buscar por pedido, cliente o email..."
              className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder disabled:opacity-60"
            />
          </div>
          <label htmlFor="admin-orders-sort" className="text-sm font-medium text-wf-ink-secondary">
            Ordenar:
          </label>
          <select
            id="admin-orders-sort"
            aria-label="Ordenar pedidos"
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

      {loadError ? (
        <p className="px-5 pb-4 text-sm text-red-600" role="alert">
          {loadError}
        </p>
      ) : null}

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
                <button
                  type="button"
                  onClick={() => onSelect(order)}
                  aria-label={`Ver detalle del pedido ${order.id}`}
                  className="underline-offset-2 hover:text-wf-ink hover:underline"
                >
                  Ver detalle →
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
