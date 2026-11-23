import { ADMIN_RANGE_OPTIONS } from "@/features/admin/helpers/admin-range";
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
  days: number;
  onStatusChange: (status: AdminPaymentStatusFilter) => void;
  onDaysChange: (days: number) => void;
};

/** Settlement tabs and the date range selector above the payments table. */
export function AdminPaymentsFilters({
  payments,
  status,
  days,
  onStatusChange,
  onDaysChange,
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

      <div className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
        <label htmlFor="admin-payments-range" className="text-wf-ink-muted">
          Rango:
        </label>
        <select
          id="admin-payments-range"
          aria-label="Rango de pagos"
          value={days}
          onChange={(event) => onDaysChange(Number(event.target.value))}
          className="bg-transparent font-medium text-wf-ink outline-none"
        >
          {ADMIN_RANGE_OPTIONS.map((option) => (
            <option key={option.days} value={option.days}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
