import Link from "next/link";

import type { AdminActivityEntry } from "@/features/admin/types";

/**
 * Recent-activity card of the side column: header plus five 60px rows with the
 * 32×32 icon placeholder, the entry text and its relative time.
 */
export function ActivityCard({ entries }: { entries: AdminActivityEntry[] }) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="flex items-center justify-between gap-3 p-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">Actividad reciente</h2>
          <p className="text-xs text-wf-ink-muted">Movimientos de la tienda</p>
        </div>
        <Link
          href="/admin/reportes"
          className="shrink-0 text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          Ver todo →
        </Link>
      </div>

      <ul className="flex flex-col">
        {entries.map((entry) => (
          <li
            key={entry.id}
            className="flex min-h-[60px] items-center gap-3 border-b border-wf-line px-5 py-2"
          >
            <span
              aria-hidden
              className="block size-8 shrink-0 rounded-wf-sm bg-wf-subtle"
            />
            <span className="flex min-w-0 flex-1 flex-col gap-1">
              <span className="text-sm text-wf-ink-secondary">{entry.text}</span>
              <span className="text-xs text-wf-ink-muted">{entry.when}</span>
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
