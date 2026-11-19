import { RatingStars } from "@/features/product/components/rating-stars";
import type { ReviewSummary } from "@/features/review/types";

const distributionRows = [5, 4, 3, 2, 1] as const;

function formatAverage(average: number): string {
  return average.toLocaleString("es-AR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });
}

/** Score, verified count and distribution bars of the reviews section. */
export function ReviewSummaryCard({ summary }: { summary: ReviewSummary }) {
  return (
    <div className="flex w-full flex-col gap-3.5 rounded-wf-card border border-wf-line bg-wf-surface p-5 lg:w-[380px] lg:shrink-0">
      <div className="flex items-center gap-3">
        <span className="text-[44px] font-bold leading-none text-wf-ink">
          {formatAverage(summary.average)}
        </span>
        <span className="flex flex-col gap-1.5">
          <RatingStars filled={5} />
          <span className="text-xs text-wf-ink-muted">
            {summary.count} reseñas verificadas
          </span>
        </span>
      </div>

      <div aria-hidden className="h-px bg-wf-line" />

      <ul className="flex flex-col gap-2">
        {distributionRows.map((stars) => {
          const count = summary.distribution[stars] ?? 0;
          const percent = summary.count > 0 ? Math.round((count / summary.count) * 100) : 0;

          return (
            <li key={stars} className="flex items-center gap-2">
              <span className="w-11 text-xs text-wf-ink-tertiary">{stars} ★</span>
              <span className="h-2 flex-1 rounded-full bg-wf-subtle">
                <span
                  className="block h-2 rounded-full bg-wf-inverse"
                  style={{ width: `${percent}%` }}
                />
              </span>
              <span className="w-9 text-right text-xs text-wf-ink-muted">{count}</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
