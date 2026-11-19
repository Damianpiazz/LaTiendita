import type { AdminProductStatus } from "@/features/admin/types";
import { cn } from "@/lib/utils";

const statusStyles: Record<
  AdminProductStatus,
  { label: string; className: string }
> = {
  publicado: {
    label: "Publicado",
    className: "border border-wf-inverse bg-wf-inverse text-wf-on-inverse",
  },
  borrador: {
    label: "Borrador",
    className:
      "border border-wf-line-strong bg-wf-surface text-wf-ink-muted",
  },
  agotado: {
    label: "Agotado",
    className:
      "border border-wf-line-strong bg-wf-surface text-wf-ink-placeholder",
  },
};

/** 24px pill of the products table, in its three states. */
export function ProductStatusBadge({
  status,
}: {
  status: AdminProductStatus;
}) {
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
