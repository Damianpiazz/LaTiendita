import Link from "next/link";

import type { AdminSummary } from "@/features/admin/types";

/**
 * Page header of the summary screen: title and subtitle on the left, period
 * control and `+ Nuevo producto` on the right.
 *
 * The period control is presentational — the frame draws no menu for it, same
 * as the sort control of the catalog screens. The creation form is out of
 * scope, so the action points at the products screen.
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
        <span className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
          <span className="text-wf-ink-muted">Período:</span>
          <span className="font-medium text-wf-ink">Últimos 7 días</span>
          <span aria-hidden className="text-wf-ink-placeholder">
            ↓
          </span>
        </span>

        <Link
          href="/admin/productos"
          className="flex h-10 items-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse transition-colors hover:bg-wf-ink"
        >
          + Nuevo producto
        </Link>
      </div>
    </header>
  );
}
