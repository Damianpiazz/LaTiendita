type AdminProductsBulkBarProps = {
  count: number;
  onPublish: () => void;
  onArchive: () => void;
  onDelete: () => void;
  onClear: () => void;
};

/** Bulk bar of the products screen; always visible with the live count. */
export function AdminProductsBulkBar({
  count,
  onPublish,
  onArchive,
  onDelete,
  onClear,
}: AdminProductsBulkBarProps) {
  return (
    <div className="flex min-h-14 flex-wrap items-center justify-between gap-3 rounded-wf-md border border-wf-line-strong bg-wf-canvas px-5 py-2">
      <div className="flex items-center gap-3">
        <span className="flex size-6 items-center justify-center rounded-full bg-wf-inverse text-xs font-bold text-wf-on-inverse">
          {count}
        </span>
        <span className="text-sm font-medium text-wf-ink-secondary">
          {count} productos seleccionados
        </span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {[
          { label: "Publicar", onClick: onPublish },
          { label: "Archivar", onClick: onArchive },
          { label: "Eliminar", onClick: onDelete },
        ].map((action) => (
          <button
            key={action.label}
            type="button"
            onClick={action.onClick}
            disabled={count === 0}
            className="flex h-8 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-xs font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:opacity-60"
          >
            {action.label}
          </button>
        ))}
        <button
          type="button"
          onClick={onClear}
          disabled={count === 0}
          className="px-1 text-xs text-wf-ink-muted transition-colors hover:text-wf-ink disabled:cursor-not-allowed disabled:opacity-60"
        >
          Limpiar
        </button>
      </div>
    </div>
  );
}
