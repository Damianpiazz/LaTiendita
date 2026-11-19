import { RatingStars } from "@/features/product/components/rating-stars";
import { formatReviewDate } from "@/features/review/helpers/format-review-date";
import type { Review } from "@/features/review/types";

/** One approved review card. */
export function ReviewCard({ review }: { review: Review }) {
  return (
    <article className="flex flex-col gap-2.5 rounded-wf-card border border-wf-line bg-wf-surface p-5">
      <div className="flex items-center gap-2.5">
        <span aria-hidden className="size-9 shrink-0 rounded-full bg-wf-muted" />
        <span className="flex flex-col gap-0.5">
          <span className="text-sm font-bold text-wf-ink">{review.author}</span>
          <span className="text-xs text-wf-ink-muted">
            {review.verified ? "Compra verificada · " : ""}
            {formatReviewDate(review.createdAt)}
          </span>
        </span>
      </div>

      <RatingStars filled={review.rating} size="sm" />

      <h3 className="text-sm font-bold text-wf-ink">{review.title}</h3>

      <p className="text-[13px] leading-5 text-wf-ink-secondary">{review.body}</p>

      <div className="flex flex-wrap items-center gap-4 text-xs">
        <span className="text-wf-ink-muted">¿Te sirvió?</span>
        <span className="font-medium text-wf-ink-secondary">
          Sí ({review.helpfulCount})
        </span>
        <span className="font-medium text-wf-ink-secondary">
          No ({review.notHelpfulCount})
        </span>
      </div>
    </article>
  );
}
