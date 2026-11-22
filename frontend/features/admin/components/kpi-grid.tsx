import { formatDelta } from "@/features/admin/helpers/sales-series";
import type { AdminSummary } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Four KPI cards, in the frame order.
 *
 * Values and meta lines are derived from the summary; the stock count is the
 * number of seeded low-stock rows.
 */
export function KpiGrid({ summary }: { summary: AdminSummary }) {
  const salesDeltaLabel = `${summary.salesDelta >= 0 ? "+" : ""}${formatDelta(summary.salesDelta)}% vs. ayer`;
  const cards = [
    {
      label: "Ventas de hoy",
      value: formatPrice(summary.salesToday),
      meta: `${salesDeltaLabel} · ${summary.ordersToday} pedidos`,
    },
    {
      label: "Pedidos pendientes",
      value: String(summary.pendingOrders),
      meta: `${summary.pendingWithoutPayment} sin pago acreditado`,
    },
    {
      label: "Pagos a acreditar",
      value: formatPrice(summary.paymentsToClear),
      meta: `${summary.paymentsInReview} pagos en revisión`,
    },
    {
      label: "Stock bajo",
      value: String(summary.lowStockTotal),
      meta: `${summary.lowStockTotal} productos con menos de ${summary.lowStockThreshold} u.`,
    },
  ];

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
      {cards.map((card) => (
        <div
          key={card.label}
          className="flex flex-col gap-2 rounded-wf-md border border-wf-line bg-wf-surface p-5"
        >
          <span className="text-sm text-wf-ink-muted">{card.label}</span>
          <span className="text-2xl font-bold text-wf-ink">{card.value}</span>
          <span className="text-xs text-wf-ink-muted">{card.meta}</span>
        </div>
      ))}
    </div>
  );
}
