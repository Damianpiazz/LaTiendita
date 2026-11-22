import type { AdminClients } from "@/features/admin/types";

/**
 * Page header of the clients screen: title, the store totals and the
 * export/segments actions.
 *
 * The copy stays exactly as drawn in the wireframe; the two actions are now
 * real controls driven by the clients workspace.
 */
export function AdminClientsHeader({
  clients,
  onExport,
  onSegments,
  segmentsOpen = false,
  exporting = false,
}: {
  clients: AdminClients;
  onExport: () => void;
  onSegments: () => void;
  segmentsOpen?: boolean;
  exporting?: boolean;
}) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Clientes</h1>
        <p className="text-sm text-wf-ink-muted">
          {clients.totalClients} clientes · {clients.newThisMonth} nuevos este mes
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={onExport}
          disabled={exporting}
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:text-wf-ink-placeholder"
        >
          {exporting ? "Exportando…" : "Exportar CSV"}
        </button>
        <button
          type="button"
          onClick={onSegments}
          aria-haspopup="menu"
          aria-expanded={segmentsOpen}
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-muted transition-colors hover:bg-wf-canvas"
        >
          Segmentos
        </button>
      </div>
    </header>
  );
}
