import {
  averageTicket,
  barHeight,
  formatDelta,
  sumSales,
} from "@/features/admin/helpers/sales-series";
import type { AdminSummary } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";
import { cn } from "@/lib/utils";

/**
 * Seven-day sales card.
 *
 * The bars scale to the tallest amount, so the heights follow the data instead
 * of being drawn by hand. The subtitle is derived from the same series.
 */
export function SalesChart({ summary }: { summary: AdminSummary }) {
  const total = sumSales(summary.salesByDay);
  const ticket = averageTicket(total, summary.weekOrders);
  const maximum = Math.max(...summary.salesByDay.map((day) => day.amount));

  return (
    <section className="flex flex-col gap-6 rounded-wf-md border border-wf-line bg-wf-surface p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">
            Ventas de los últimos 7 días
          </h2>
          <p className="text-xs text-wf-ink-muted">
            Total {formatPrice(total)} · {summary.weekOrders} pedidos · ticket
            promedio {formatPrice(ticket)}
          </p>
        </div>
        <span className="text-xs font-medium text-wf-ink-secondary">
          +{formatDelta(summary.weekDelta)}% vs. semana anterior
        </span>
      </div>

      <div className="overflow-x-auto">
        <div className="flex h-[180px] min-w-[520px] items-end gap-3 border-b border-wf-line">
          {summary.salesByDay.map((day) => (
            <div
              key={day.label}
              className="flex h-full flex-1 flex-col items-center justify-end gap-2"
            >
              <span
                aria-hidden
                className={cn(
                  "block w-12 rounded-wf-sm",
                  day.current ? "bg-wf-inverse" : "bg-wf-subtle",
                )}
                style={{ height: `${barHeight(day.amount, maximum)}px` }}
              />
              <span
                className={cn(
                  "text-xs",
                  day.current ? "font-medium text-wf-ink" : "text-wf-ink-muted",
                )}
              >
                {day.label}
              </span>
            </div>
          ))}
        </div>

        <p className="flex items-center gap-3 text-sm text-wf-ink-secondary">
          <span className="font-medium">
            {formatPrice(ticket)} ticket promedio
          </span>
          <span className="text-wf-ink-faint">|</span>
          <span>
            {summary.weekDelta >= 0 ? "+" : ""}
            {formatDelta(summary.weekDelta)}% vs. semana anterior
          </span>
        </p>
      </div>
    </section>
  );
}
