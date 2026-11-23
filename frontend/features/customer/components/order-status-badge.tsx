import type { CustomerOrderStatus } from "@/features/customer/types";
import { cn } from "@/lib/utils";

/**
 * 24px pill of the purchases table. The wireframe draws `Entregado` and
 * `Enviado`; backend orders also arrive as pending/paid/cancelled, so those
 * states reuse the same neutral palette instead of inventing new colors.
 */
const statusStyles: Record<CustomerOrderStatus, { label: string; className: string }> = {
  pendiente: {
    label: "Pendiente",
    className: "bg-wf-subtle text-wf-ink-secondary",
  },
  pagado: {
    label: "Pagado",
    className: "border border-wf-line-strong text-wf-ink-secondary",
  },
  enviado: {
    label: "Enviado",
    className: "bg-wf-subtle text-wf-ink-secondary",
  },
  entregado: {
    label: "Entregado",
    className: "bg-wf-inverse text-wf-on-inverse",
  },
  cancelado: {
    label: "Cancelado",
    className: "border border-wf-line-strong text-wf-ink-muted",
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
