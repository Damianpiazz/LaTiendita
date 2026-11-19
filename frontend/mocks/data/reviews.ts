import type {
  Review,
  ReviewDistribution,
  ReviewSummary,
} from "@/features/review/types";

const emptyDistribution: ReviewDistribution = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };

/** `GET /reviews/summary` seed. Only the target product has reviews in the wireframe. */
export const reviewSummaries: Record<string, ReviewSummary> = {
  "auriculares-inalambricos": {
    productId: "auriculares-inalambricos",
    average: 4.8,
    count: 124,
    distribution: { 5: 96, 4: 18, 3: 6, 2: 3, 1: 1 },
  },
};

/** The two cards the wireframe shows; the other 122 reviews are not rendered. */
export const reviews: Review[] = [
  {
    id: "review-maria-g",
    productId: "auriculares-inalambricos",
    author: "María G.",
    createdAt: "2026-10-12",
    rating: 5,
    title: "Excelente sonido",
    body: "La batería dura lo que dice y la cancelación de ruido funciona muy bien en el subte. Los usé todo el día y no me dolieron.",
    verified: true,
    helpfulCount: 18,
    notHelpfulCount: 2,
  },
  {
    id: "review-lucas-p",
    productId: "auriculares-inalambricos",
    author: "Lucas P.",
    createdAt: "2026-09-28",
    rating: 4,
    title: "Muy buenos, la funda podría mejorar",
    body: "Son cómodos y el sonido está muy bien por el precio. La funda de transporte es medio floja, pero por todo lo demás va perfecto.",
    verified: true,
    helpfulCount: 9,
    notHelpfulCount: 2,
  },
];

export function getReviewSummaryMock(productId: string): ReviewSummary {
  return (
    reviewSummaries[productId] ?? {
      productId,
      average: 0,
      count: 0,
      distribution: { ...emptyDistribution },
    }
  );
}

export function getReviewsMock(productId: string, page: number, size: number): Review[] {
  const productReviews = reviews.filter((review) => review.productId === productId);
  const start = (page - 1) * size;

  return productReviews.slice(start, start + size);
}
