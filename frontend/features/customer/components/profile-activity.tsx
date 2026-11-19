import { ActivityStatusBadge } from "@/features/customer/components/activity-status-badge";
import type { CustomerActivity } from "@/features/customer/types";
import { cn } from "@/lib/utils";

/**
 * `Actividad reciente`: section head plus the four 60px rows of the frame.
 *
 * The chevron is the drawn `→` glyph and the link above the card has no
 * destination yet, so it stays presentational.
 */
export function ProfileActivity({ activity }: { activity: CustomerActivity[] }) {
  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-wf-ink">Actividad reciente</h2>
        <span className="shrink-0 text-sm text-wf-ink-secondary">
          Ver toda la actividad →
        </span>
      </div>

      <div className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
        {activity.map((entry, index) => (
          <div
            key={`${entry.date}-${entry.description}`}
            className={cn(
              "flex h-[60px] items-center gap-4 px-5",
              index < activity.length - 1 && "border-b border-wf-line",
            )}
          >
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
          </div>
        ))}
      </div>
    </section>
  );
}
