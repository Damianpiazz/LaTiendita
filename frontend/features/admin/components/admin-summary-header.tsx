import Link from "next/link";

import type { AdminSummary } from "@/features/admin/types";

/**
 * Page header of the summary screen: title and subtitle on the left, period
 * control and the link to products on the right.
 *
 * The summary endpoint only serves a fixed 7-day window, so the period control
 * is an explicitly disabled select instead of a dropdown that does nothing.
 * The creation form lives on the products screen, so the action is labelled as
 * plain navigation instead of promising to open it here.
 */
export function AdminSummaryHeader({ summary }: { summary: AdminSummary }) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Resumen</h1>
        <p className="text-sm text-wf-ink-muted">
          {summary.dateLabel} · {summary.updatedLabel}
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <div className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
          <label htmlFor="admin-summary-range" className="text-wf-ink-muted">
            Período:
          </label>
          <select
            id="admin-summary-range"
            aria-label="Período del resumen"
            disabled
            title="El resumen solo está disponible para los últimos 7 días"
            className="cursor-not-allowed bg-transparent font-medium text-wf-ink disabled:text-wf-ink-muted"
          >
            <option value="7">Últimos 7 días</option>
          </select>
        </div>

        <Link
          href="/admin/productos"
          className="flex h-10 items-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse transition-colors hover:bg-wf-ink"
        >
          Ir a productos
        </Link>
      </div>
    </header>
  );
}
