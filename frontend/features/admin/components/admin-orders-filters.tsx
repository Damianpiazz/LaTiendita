"use client";

import type { AdminOrderStatus, AdminOrderStatusFilter, AdminOrders } from "@/features/admin/types";
import { cn } from "@/lib/utils";

const tabs: { status: "todos" | AdminOrderStatus; label: string }[] = [
  { status: "todos", label: "Todos" },
  { status: "pendiente", label: "Pendientes" },
  { status: "pagado", label: "Pagados" },
  { status: "enviado", label: "Enviados" },
  { status: "entregado", label: "Entregados" },
  { status: "cancelado", label: "Cancelados" },
];

type Props = {
  orders: AdminOrders;
  status: AdminOrderStatusFilter;
  onStatusChange: (status: AdminOrderStatusFilter) => void;
};

/** Status tabs and the period control above the orders table. */
export function AdminOrdersFilters({ orders, status, onStatusChange }: Props) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div className="flex flex-wrap items-center gap-2">
        {tabs.map((tab) => {
          const value: AdminOrderStatusFilter = tab.status === "todos" ? "all" : tab.status;
          const active = value === status;

          return (
            <button
              key={tab.status}
              type="button"
              aria-current={active ? "true" : undefined}
              onClick={() => onStatusChange(value)}
              className={cn(
                "flex h-10 items-center gap-2 rounded-full px-4 text-sm",
                active
                  ? "bg-wf-inverse font-medium text-wf-on-inverse"
                  : "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
              )}
            >
              <span>{tab.label}</span>
              <span
                className={cn(
                  "text-xs font-bold",
                  active ? "text-wf-on-inverse" : "text-wf-ink-muted",
                )}
              >
                {orders.statusCounts[tab.status]}
              </span>
            </button>
          );
        })}
      </div>

      <span className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
        <span className="text-wf-ink-muted">Rango:</span>
        <span className="font-medium text-wf-ink">{orders.rangeLabel}</span>
        <span aria-hidden className="text-wf-ink-placeholder">
          ↓
        </span>
      </span>
    </div>
  );
}
