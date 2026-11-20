import { cacheLife } from "next/cache";

import { env } from "@/config/env";
import type {
  Review,
  ReviewDistribution,
  ReviewPage,
  ReviewSummary,
  StarRating,
} from "@/features/review/types";
import { httpClient } from "@/lib/http/client";

type ReviewPageOptions = {
  page?: number;
  size?: number;
};

/** `ReviewSummaryDto` of review-service: decimal string and string star keys. */
type ReviewSummaryDto = {
  productId: string | number;
  average: string | number;
  reviewCount: number;
  distribution: Record<string, number>;
};

/** `ReviewDto` of review-service, restricted to the fields the screen renders. */
type ReviewDto = {
  id: string;
  productId: string | number;
  customerId: string;
  rating: number;
  title: string;
  body: string;
  status: string;
  helpfulVotes: number;
  createdAt: string;
};

/** Spring `Page<ReviewDto>` served by `GET /api/reviews`. */
type SpringPage<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
};

const STAR_RATINGS: StarRating[] = [1, 2, 3, 4, 5];

function toStarRating(value: number): StarRating {
  return Math.min(5, Math.max(1, value)) as StarRating;
}

function toReviewSummary(dto: ReviewSummaryDto): ReviewSummary {
  const distribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 } as ReviewDistribution;
  for (const star of STAR_RATINGS) {
    distribution[star] = Number(dto.distribution?.[String(star)] ?? 0);
  }

  return {
    productId: String(dto.productId),
    average: Number(dto.average),
    count: Number(dto.reviewCount),
    distribution,
  };
}

function toReview(dto: ReviewDto): Review {
  return {
    id: String(dto.id),
    productId: String(dto.productId),
    // The DTO identifies the customer but carries no display name.
    author: "Cliente verificado",
    createdAt: dto.createdAt,
    rating: toStarRating(dto.rating),
    title: dto.title,
    body: dto.body,
    verified: dto.status === "approved",
    helpfulCount: Number(dto.helpfulVotes ?? 0),
    notHelpfulCount: 0,
  };
}

export async function getReviewSummary(productId: string): Promise<ReviewSummary> {
  "use cache";
  cacheLife("hours");

  // The mock adapter serves `/reviews/*`; the gateway exposes `/api/reviews/*`.
  const path = env.useMocks ? "/reviews/summary" : "/api/reviews/summary";
  const { data } = await httpClient.get<ReviewSummary | ReviewSummaryDto>(path, {
    params: { productId },
  });

  return env.useMocks ? (data as ReviewSummary) : toReviewSummary(data as ReviewSummaryDto);
}

export async function getReviews(
  productId: string,
  { page = 1, size = 10 }: ReviewPageOptions = {},
): Promise<ReviewPage> {
  "use cache";
  cacheLife("hours");

  // The screen and the mock adapter use 1-based pages; Spring uses 0-based.
  const apiPage = env.useMocks ? page : Math.max(0, page - 1);
  const path = env.useMocks ? "/reviews" : "/api/reviews";
  const { data } = await httpClient.get<ReviewPage | SpringPage<ReviewDto>>(path, {
    params: { productId, page: apiPage, size },
  });

  if (env.useMocks) {
    return data as ReviewPage;
  }

  const springPage = data as SpringPage<ReviewDto>;
  return {
    items: (springPage.content ?? []).map(toReview),
    page: springPage.number + 1,
    size: springPage.size,
    total: springPage.totalElements,
  };
}
