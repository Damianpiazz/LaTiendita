import type { AdminNavigationCounts } from "@/features/admin/types";

/**
 * Topbar of the admin shell, measured from the `Topbar` frame: 72 tall, 32px
 * gutters, white with a bottom hairline.
 *
 * Search and `Avisos` are presentational: the frame only draws their resting
 * state, so they ship as the same kind of control without a menu behind it.
 */
export function AdminTopbar({ counts }: { counts: AdminNavigationCounts }) {
  return (
    <header className="flex h-[72px] shrink-0 items-center justify-between gap-4 border-b border-wf-line bg-wf-surface px-8">
      <div
        role="search"
        className="hidden h-10 w-[320px] items-center rounded-wf-sm border border-wf-line bg-wf-canvas px-4 md:flex"
      >
        <label htmlFor="admin-search" className="sr-only">
          Buscar en la tienda
        </label>
        <input
          id="admin-search"
          type="search"
          placeholder="Buscar pedidos, productos o clientes..."
          className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
        />
      </div>

      <div className="flex items-center gap-3">
        <span className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm text-wf-ink-secondary">
          Avisos
          <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-wf-subtle px-2 text-xs font-bold text-wf-ink-secondary">
            {counts.alerts}
          </span>
        </span>

        <span className="flex size-10 items-center justify-center rounded-full bg-wf-subtle text-sm font-bold text-wf-ink-secondary">
          DP
        </span>
      </div>
    </header>
  );
}
