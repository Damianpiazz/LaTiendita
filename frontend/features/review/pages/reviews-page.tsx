import Link from "next/link";
import { notFound } from "next/navigation";

import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { findProductBySlug } from "@/features/product/services/catalog.service";
import { ReviewCard } from "@/features/review/components/review-card";
import { ReviewSummaryCard } from "@/features/review/components/review-summary-card";
import {
  getReviewSummary,
  getReviews,
} from "@/features/review/services/review.service";

const PAGE_SIZE = 6;

function reviewsHref(slug: string, page: number): string {
  return `/productos/${encodeURIComponent(slug)}/resenas?page=${page}`;
}

/** Full, paginated reviews view of a product. */
export async function ReviewsPage({
  slug,
  page,
}: {
  slug: string;
  page: number;
}) {
  const product = await findProductBySlug(slug);
  if (!product) {
    notFound();
  }

  // Both reads depend only on the product id, so start them together.
  const [summary, reviewPage] = await Promise.all([
    getReviewSummary(product.id),
    getReviews(product.id, { page: Math.max(1, page), size: PAGE_SIZE }),
  ]);
  const pageCount = Math.max(1, Math.ceil(reviewPage.total / PAGE_SIZE));
  const current = Math.min(Math.max(1, page), pageCount);

  const pageNumbers = Array.from({ length: pageCount }, (_, index) => index + 1);

  return (
    <main>
      <Breadcrumbs
        items={[
          { label: "Inicio", href: "/" },
          { label: product.name, href: `/productos/${product.slug}` },
          { label: "Reseñas" },
        ]}
      />

      <section className="bg-wf-canvas p-12">
        <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-8">
          <h1 className="text-2xl font-bold text-wf-ink">
            Reseñas ({summary.count})
          </h1>

          <div className="flex flex-col gap-6 lg:flex-row">
            <ReviewSummaryCard summary={summary} />

            {reviewPage.items.length > 0 ? (
              <div className="grid flex-1 gap-6 md:grid-cols-2">
                {reviewPage.items.map((review) => (
                  <ReviewCard key={review.id} review={review} />
                ))}
              </div>
            ) : (
              <p className="flex-1 text-sm text-wf-ink-muted">
                No hay reseñas en esta página.
              </p>
            )}
          </div>

          {pageCount > 1 ? (
            <nav aria-label="Páginas de reseñas" className="flex items-center justify-center gap-2">
              {pageNumbers.map((number) =>
                number === current ? (
                  <span
                    key={number}
                    aria-current="page"
                    className="flex size-10 items-center justify-center rounded-wf-sm border border-wf-inverse bg-wf-inverse text-sm font-medium text-wf-on-inverse"
                  >
                    {number}
                  </span>
                ) : (
                  <Link
                    key={number}
                    href={reviewsHref(slug, number)}
                    aria-label={String(number)}
                    className="flex size-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm text-wf-ink-secondary transition-colors hover:text-wf-ink"
                  >
                    {number}
                  </Link>
                ),
              )}
            </nav>
          ) : null}
        </div>
      </section>
    </main>
  );
}
