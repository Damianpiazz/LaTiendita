import { cacheLife } from "next/cache";

import type { ReviewPage, ReviewSummary } from "@/features/review/types";
import { httpClient } from "@/lib/http/client";

type ReviewPageOptions = {
  page?: number;
  size?: number;
};

export async function getReviewSummary(productId: string): Promise<ReviewSummary> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<ReviewSummary>("/reviews/summary", {
    params: { productId },
  });

  return data;
}

export async function getReviews(
  productId: string,
  { page = 1, size = 10 }: ReviewPageOptions = {},
): Promise<ReviewPage> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<ReviewPage>("/reviews", {
    params: { productId, page, size },
  });

  return data;
}
