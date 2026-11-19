import type { CustomerOrderStatus } from "@/features/customer/types";
import { cn } from "@/lib/utils";

/**
 * 24px pill of the purchases table, in the two states the frame draws.
 *
 * The activity rows use a wider vocabulary (`Publicada`, `Actualizado`), so the
 * two badges stay separate instead of sharing one map.
 */
const statusStyles: Record<CustomerOrderStatus, { label: string; className: string }> = {
  entregado: {
    label: "Entregado",
    className: "bg-wf-inverse text-wf-on-inverse",
  },
  enviado: {
    label: "Enviado",
    className: "bg-wf-subtle text-wf-ink-secondary",
  },
};

export function OrderStatusBadge({ status }: { status: CustomerOrderStatus }) {
  const style = statusStyles[status];

  return (
    <span
      className={cn(
        "inline-flex h-6 items-center rounded-full px-2 text-xs",
        style.className,
      )}
    >
      {style.label}
    </span>
  );
}
