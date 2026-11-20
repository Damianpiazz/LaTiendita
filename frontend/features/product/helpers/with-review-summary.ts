import type { ProductDetail } from "@/features/product/types";
import type { ReviewSummary } from "@/features/review/types";

/**
 * `product-service` does not expose rating or review count; `review-service`
 * owns them. Apply the summary after both requests so the detail screen and the
 * reviews section always show the same figures.
 */
export function withReviewSummary(
  product: ProductDetail,
  summary: ReviewSummary,
): ProductDetail {
  return {
    ...product,
    rating: summary.average,
    reviewCount: summary.count,
  };
}
