import type { AdminPayments } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Four KPI cards of the payments screen, in the frame order.
 *
 * Volumes come from the seed; the caption counts and the approval rate are
 * derived from the settlement totals so the row keeps adding up.
 */
export function AdminPaymentsKpis({ payments }: { payments: AdminPayments }) {
  const cards = [
    {
      label: "Cobrado (30 días)",
      value: formatPrice(payments.approvedVolume),
      meta: `${payments.approvedCount} pagos acreditados`,
    },
    {
      label: "Pendiente de acreditación",
      value: formatPrice(payments.pendingVolume),
      meta: `${payments.pendingCount} pagos en proceso`,
    },
    {
      label: "Rechazados",
      value: formatPrice(payments.rejectedVolume),
      meta: `${payments.rejectedCount} pagos para revisar`,
    },
    {
      label: "Tasa de aprobación",
      value: `${payments.approvalRate}%`,
      meta: `${payments.approvedCount} de ${payments.approvalAttempts} intentos`,
    },
  ];

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => (
        <div
          key={card.label}
          className="flex flex-col gap-2 rounded-wf-md border border-wf-line bg-wf-surface p-5"
        >
          <span className="text-xs text-wf-ink-muted">{card.label}</span>
          <span className="text-2xl font-bold text-wf-ink">{card.value}</span>
          <span className="text-xs text-wf-ink-muted">{card.meta}</span>
        </div>
      ))}
    </div>
  );
}
