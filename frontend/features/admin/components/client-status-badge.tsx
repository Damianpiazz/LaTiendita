import type { AdminClientStatus } from "@/features/admin/types";
import { cn } from "@/lib/utils";

/**
 * 24px pill of the clients table.
 *
 * `Nueva` and `Nuevo` are both in the vocabulary because the frame prints the
 * feminine form for Lucía and the masculine one for Julián and Nicolás for the
 * same state; normalising them would change the design's copy.
 */
const statusStyles: Record<
  AdminClientStatus,
  { label: string; className: string }
> = {
  nueva: {
    label: "Nueva",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
  },
  nuevo: {
    label: "Nuevo",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
  },
  recurrente: {
    label: "Recurrente",
    className: "border border-wf-inverse bg-wf-inverse text-wf-on-inverse",
  },
  activa: {
    label: "Activa",
    className: "border border-wf-subtle bg-wf-subtle text-wf-ink-secondary",
  },
  inactiva: {
    label: "Inactiva",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-muted",
  },
  "sin-compras": {
    label: "Inactiva",
    className: "border border-wf-line-strong bg-wf-surface text-wf-ink-muted",
  },
};

export function ClientStatusBadge({ status }: { status: AdminClientStatus }) {
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
