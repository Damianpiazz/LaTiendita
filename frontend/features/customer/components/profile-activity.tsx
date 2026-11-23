import Link from "next/link";

import { ActivityStatusBadge } from "@/features/customer/components/activity-status-badge";
import type { CustomerActivity } from "@/features/customer/types";
import { cn } from "@/lib/utils";

/** `Actividad reciente`: section head plus the rows of the frame. */
export function ProfileActivity({ activity }: { activity: CustomerActivity[] }) {
  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-2xl leading-[29px] font-bold text-wf-ink">Actividad reciente</h2>
        <Link
          href="/mi-cuenta/actividad"
          className="shrink-0 text-sm leading-[17px] text-wf-ink-secondary underline-offset-2 hover:text-wf-ink hover:underline"
        >
          Ver toda la actividad →
        </Link>
      </div>

      <div className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
        {activity.length === 0 ? (
          <p className="flex h-16 items-center justify-center px-5 text-sm text-wf-ink-muted">
            Todavía no hay actividad registrada.
          </p>
        ) : null}
        {activity.map((entry, index) => {
          const content = (
            <>
              <span className="w-24 shrink-0 text-xs text-wf-ink-muted">
                {entry.date}
              </span>
              <span className="min-w-0 flex-1 truncate text-sm text-wf-ink-secondary">
                {entry.description}
              </span>
              <ActivityStatusBadge status={entry.status} />
              <span aria-hidden className="text-sm text-wf-ink-placeholder">
                →
              </span>
            </>
          );
          const rowClass = cn(
            "flex h-[60px] items-center gap-4 px-5",
            index < activity.length - 1 && "border-b border-wf-line",
            entry.href && "transition-colors hover:bg-wf-canvas",
          );

          return entry.href ? (
            <Link key={`${entry.date}-${entry.description}`} href={entry.href} className={rowClass}>
              {content}
            </Link>
          ) : (
            <div key={`${entry.date}-${entry.description}`} className={rowClass}>
              {content}
            </div>
          );
        })}
      </div>
    </section>
  );
}
