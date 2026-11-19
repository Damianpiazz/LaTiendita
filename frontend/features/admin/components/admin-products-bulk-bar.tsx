/**
 * Bulk bar of the products screen.
 *
 * The frame always draws it with two selected rows, so the component renders
 * from the seed count instead of disappearing when nothing is selected.
 */
export function AdminProductsBulkBar({ count }: { count: number }) {
  return (
    <div className="flex min-h-14 flex-wrap items-center justify-between gap-3 rounded-wf-md border border-wf-line-strong bg-wf-canvas px-5 py-2">
      <div className="flex items-center gap-3">
        <span className="flex size-6 items-center justify-center rounded-full bg-wf-inverse text-xs font-bold text-wf-on-inverse">
          {count}
        </span>
        <span className="text-sm font-medium text-wf-ink-secondary">
          productos seleccionados
        </span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {["Publicar", "Archivar", "Eliminar"].map((action) => (
          <span
            key={action}
            className="flex h-8 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-xs font-medium text-wf-ink-secondary"
          >
            {action}
          </span>
        ))}
        <span className="px-1 text-xs text-wf-ink-muted">Limpiar</span>
      </div>
    </div>
  );
}
