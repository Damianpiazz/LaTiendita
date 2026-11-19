export type StarRating = 1 | 2 | 3 | 4 | 5;

/** Approved review count per star, as stored in `product_ratings.distribution`. */
export type ReviewDistribution = Record<StarRating, number>;

/** `ReviewSummaryDto` of `review-service`. */
export type ReviewSummary = {
  productId: string;
  average: number;
  count: number;
  distribution: ReviewDistribution;
};

/** `ReviewDto` of `review-service`, restricted to the fields the screen renders. */
export type Review = {
  id: string;
  productId: string;
  author: string;
  /** ISO date; the UI formats it for display. */
  createdAt: string;
  rating: StarRating;
  title: string;
  body: string;
  verified: boolean;
  helpfulCount: number;
  notHelpfulCount: number;
};

/** Paginated `GET /reviews` response. */
export type ReviewPage = {
  items: Review[];
  page: number;
  size: number;
  total: number;
};
