import type {
  AdminPaymentStatus,
  AdminPaymentStatusFilter,
  AdminPayments,
} from "@/features/admin/types";
import { cn } from "@/lib/utils";

type TabStatus = "todos" | AdminPaymentStatus;

const tabs: { status: TabStatus; label: string }[] = [
  { status: "todos", label: "Todos" },
  { status: "aprobado", label: "Aprobados" },
  { status: "pendiente", label: "Pendientes" },
  { status: "rechazado", label: "Rechazados" },
];

const counts = (payments: AdminPayments) => ({
  todos: payments.totalPayments,
  aprobado: payments.approvedCount,
  pendiente: payments.pendingCount,
  rechazado: payments.rejectedCount,
});

type AdminPaymentsFiltersProps = {
  payments: AdminPayments;
  status: AdminPaymentStatusFilter;
  onStatusChange: (status: AdminPaymentStatusFilter) => void;
};

/**
 * Settlement tabs and the 30-day period control above the payments table.
 *
 * Counts come from the real summary; the period control stays presentational
 * because the frame draws no menu and the adapter always requests 30 days.
 */
export function AdminPaymentsFilters({
  payments,
  status,
  onStatusChange,
}: AdminPaymentsFiltersProps) {
  const tabCounts = counts(payments);

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div className="flex flex-wrap items-center gap-2">
        {tabs.map((tab) => {
          const active = tab.status === status;

          return (
            <button
              key={tab.status}
              type="button"
              aria-current={active ? "true" : undefined}
              onClick={() => onStatusChange(tab.status)}
              className={cn(
                "flex h-10 items-center gap-2 rounded-full px-4 text-sm transition-colors",
                active
                  ? "bg-wf-inverse font-medium text-wf-on-inverse"
                  : "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
              )}
            >
              <span>{tab.label}</span>
              <span
                className={cn(
                  "text-xs font-bold",
                  active ? "text-wf-on-inverse" : "text-wf-ink-muted",
                )}
              >
                {tabCounts[tab.status]}
              </span>
            </button>
          );
        })}
      </div>

      <span className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
        <span className="text-wf-ink-muted">Rango:</span>
        <span className="font-medium text-wf-ink">{payments.rangeLabel}</span>
        <span aria-hidden className="text-wf-ink-placeholder">
          ↓
        </span>
      </span>
    </div>
  );
}
