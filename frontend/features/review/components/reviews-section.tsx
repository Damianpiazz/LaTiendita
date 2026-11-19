import Link from "next/link";

import type { ProductDetail } from "@/features/product/types";
import { ReviewCard } from "@/features/review/components/review-card";
import { ReviewSummaryCard } from "@/features/review/components/review-summary-card";
import type { Review, ReviewSummary } from "@/features/review/types";

type ReviewsSectionProps = {
  product: ProductDetail;
  summary: ReviewSummary;
  reviews: Review[];
};

/** Reviews section measured from the `Reviews` frame (0:352). */
export function ReviewsSection({ product, summary, reviews }: ReviewsSectionProps) {
  return (
    <section id="resenas" aria-labelledby="resenas-titulo" className="bg-wf-surface p-12">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <h2 id="resenas-titulo" className="text-2xl font-bold text-wf-ink">
            Reseñas ({summary.count})
          </h2>
          <span className="inline-flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-[18px] text-sm font-medium text-wf-ink-secondary">
            Escribir una reseña
          </span>
        </div>

        <div className="flex flex-col gap-6 lg:flex-row">
          <ReviewSummaryCard summary={summary} />

          {reviews.length > 0 ? (
            <div className="grid flex-1 gap-6 md:grid-cols-2">
              {reviews.map((review) => (
                <ReviewCard key={review.id} review={review} />
              ))}
            </div>
          ) : (
            <p className="flex-1 text-sm text-wf-ink-muted">
              Este producto todavía no tiene reseñas.
            </p>
          )}
        </div>

        {summary.count > 0 ? (
          <Link
            href={`/productos/${product.slug}/resenas`}
            className="self-start text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
          >
            Ver las {summary.count} reseñas →
          </Link>
        ) : null}
      </div>
    </section>
  );
}
