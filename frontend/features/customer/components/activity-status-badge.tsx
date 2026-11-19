import type { CustomerActivityStatus } from "@/features/customer/types";
import { cn } from "@/lib/utils";

/**
 * 24px pill of the activity rows.
 *
 * `Publicada` reuses the subtle fill of `Enviado` and `Actualizado` is the only
 * outlined state, exactly as the frame draws them; the activity vocabulary does
 * not overlap the order one, so it gets its own badge.
 */
const statusStyles: Record<CustomerActivityStatus, { label: string; className: string }> = {
  entregado: {
    label: "Entregado",
    className: "bg-wf-inverse text-wf-on-inverse",
  },
  enviado: {
    label: "Enviado",
    className: "bg-wf-subtle text-wf-ink-secondary",
  },
  publicada: {
    label: "Publicada",
    className: "bg-wf-subtle text-wf-ink-secondary",
  },
  actualizado: {
    label: "Actualizado",
    className: "border border-wf-line-strong text-wf-ink-secondary",
  },
};

export function ActivityStatusBadge({ status }: { status: CustomerActivityStatus }) {
  const style = statusStyles[status];

  return (
    <span
      className={cn(
        "inline-flex h-6 shrink-0 items-center rounded-full px-2 text-xs",
        style.className,
      )}
    >
      {style.label}
    </span>
  );
}
