import type { AdminPaymentStatus } from "@/features/admin/types";
import { cn } from "@/lib/utils";

const statusStyles: Record<
  AdminPaymentStatus,
  { label: string; className: string }
> = {
  aprobado: {
    label: "Aprobado",
    className: "border border-wf-inverse bg-wf-inverse text-wf-on-inverse",
  },
  pendiente: {
    label: "Pendiente",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
  },
  rechazado: {
    label: "Rechazado",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-muted",
  },
};

/**
 * 24px pill of the payments table, in its three settlement states.
 *
 * The orders `StatusBadge` maps a different vocabulary (`Pagado`, `Entregado`,
 * …), so payments get their own instead of stretching that one.
 */
export function PaymentStatusBadge({ status }: { status: AdminPaymentStatus }) {
  const style = statusStyles[status];

  return (
    <span
      className={cn(
        "inline-flex h-6 items-center rounded-full px-2 text-xs font-medium",
        style.className,
      )}
    >
      {style.label}
    </span>
  );
}
