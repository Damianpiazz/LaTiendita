"use client";

import { useId } from "react";

type AdminProductConfirmDialogProps = {
  kind: "archive" | "delete";
  count: number;
  onCancel: () => void;
  onConfirm: () => void;
};

/** Confirm action of the design system, reused by archiving and deletion. */
export function AdminProductConfirmDialog({
  kind,
  count,
  onCancel,
  onConfirm,
}: AdminProductConfirmDialogProps) {
  const titleId = useId();
  const title =
    kind === "archive" ? "Archivar productos" : "Eliminar productos";
  const headline = `${title.replace(" productos", "")} ${count} productos`;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div aria-hidden className="absolute inset-0 bg-wf-inverse/45" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className="relative flex max-h-[calc(100vh-2rem)] w-full max-w-[480px] flex-col overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface"
      >
        <div className="min-h-0 flex-1 overflow-y-auto p-8">
          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-1">
              <h2 id={titleId} className="text-2xl font-bold text-wf-ink">
                Confirmar accion
              </h2>
              <p className="text-sm text-wf-ink-muted">
                Esta accion afecta a {count} productos seleccionados.
              </p>
            </div>
            <div className="rounded-wf-sm bg-wf-canvas p-4">
              <p className="text-base font-medium text-wf-ink">{headline}</p>
              <p className="mt-2 text-sm text-wf-ink-secondary">
                {kind === "archive"
                  ? "Los productos dejan de estar publicados y no aparecen en la tienda. Podés revertir esta acción desde la tabla."
                  : "Los productos se eliminan del catálogo. Esta acción no se puede deshacer."}
              </p>
            </div>
          </div>
        </div>
        <footer className="flex shrink-0 items-center justify-end gap-3 border-t border-wf-line px-8 py-4">
          <button
            type="button"
            onClick={onCancel}
            className="flex h-10 w-[120px] items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="flex h-10 min-w-[160px] items-center justify-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse"
          >
            {title}
          </button>
        </footer>
      </div>
    </div>
  );
}
