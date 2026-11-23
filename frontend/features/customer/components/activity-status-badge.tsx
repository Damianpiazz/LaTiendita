import type { CustomerActivityStatus } from "@/features/customer/types";
import { cn } from "@/lib/utils";

/**
 * 24px pill of the activity rows. `Publicada` reuses the subtle fill of
 * `Enviado` and the remaining states stay outlined, exactly as the frame draws
 * them; order states share the same neutral vocabulary here.
 */
const statusStyles: Record<CustomerActivityStatus, { label: string; className: string }> = {
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
  publicada: {
    label: "Publicada",
    className: "bg-wf-subtle text-wf-ink-secondary",
  },
  rechazada: {
    label: "Rechazada",
    className: "border border-wf-line-strong text-wf-ink-muted",
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
