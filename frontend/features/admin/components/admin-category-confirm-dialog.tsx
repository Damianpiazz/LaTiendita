"use client";

import { useId } from "react";

import type { AdminCategory } from "@/features/admin/types";

type AdminCategoryConfirmDialogProps = {
  category: AdminCategory;
  error?: string | null;
  onCancel: () => void;
  onConfirm: () => void;
};

/** Confirm action of the categories screen, built from the approved frame. */
export function AdminCategoryConfirmDialog({
  category,
  error,
  onCancel,
  onConfirm,
}: AdminCategoryConfirmDialogProps) {
  const titleId = useId();

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
                Eliminar categoría
              </h2>
              <p className="text-sm text-wf-ink-muted">
                Revisá el impacto antes de continuar.
              </p>
            </div>

            <div className="flex flex-col gap-2 rounded-wf-sm bg-wf-canvas p-4">
              <p className="text-base font-medium text-wf-ink">
                Eliminar {category.name}
              </p>
              <p className="text-sm text-wf-ink-secondary">
                {`Los ${category.productCount} productos dejarán de estar asociados y pasarán a Sin categoría. La categoría y su URL /categorias/${category.slug} dejarán de estar disponibles.`}
              </p>
            </div>

            <div className="flex items-start gap-2">
              <span aria-hidden className="font-bold text-wf-ink-secondary">
                !
              </span>
              <p className="text-sm text-wf-ink-secondary">
                Esta acción no se puede deshacer.
              </p>
            </div>

            {error ? (
              <p role="alert" className="text-sm text-wf-ink">
                {error}
              </p>
            ) : null}
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
            Eliminar categoría
          </button>
        </footer>
      </div>
    </div>
  );
}
