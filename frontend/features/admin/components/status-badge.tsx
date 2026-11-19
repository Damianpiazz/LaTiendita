import type { AdminOrderStatus } from "@/features/admin/types";
import { cn } from "@/lib/utils";

const statusStyles: Record<
  AdminOrderStatus,
  { label: string; className: string }
> = {
  pendiente: {
    label: "Pendiente",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
  },
  pagado: {
    label: "Pagado",
    className: "border border-wf-inverse bg-wf-inverse text-wf-on-inverse",
  },
  enviado: {
    label: "Enviado",
    className: "border border-wf-subtle bg-wf-subtle text-wf-ink-secondary",
  },
  entregado: {
    label: "Entregado",
    className: "border border-wf-line bg-wf-surface text-wf-ink-muted",
  },
  cancelado: {
    label: "Cancelado",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-muted",
  },
};

/** 24px pill of the orders table, in its five states. */
export function StatusBadge({ status }: { status: AdminOrderStatus }) {
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
