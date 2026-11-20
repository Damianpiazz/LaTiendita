import type { Metadata } from "next";

import { ReviewsPage } from "@/features/review/pages/reviews-page";
import { findProductBySlug } from "@/features/product/services/catalog.service";

// The reviews page depends on the requested page and live review data.
export const instant = false;

type ReviewsRouteProps = {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

export async function generateMetadata({
  params,
}: ReviewsRouteProps): Promise<Metadata> {
  const { slug } = await params;
  const product = await findProductBySlug(slug);

  return product ? { title: `Reseñas · ${product.name}` } : {};
}

export default async function Page({
  params,
  searchParams,
}: ReviewsRouteProps) {
  const { slug } = await params;
  const query = await searchParams;
  const rawPage = Array.isArray(query.page) ? query.page[0] : query.page;
  const parsedPage = Number.parseInt(rawPage ?? "1", 10);
  const page = Number.isFinite(parsedPage) && parsedPage > 0 ? parsedPage : 1;

  return <ReviewsPage slug={slug} page={page} />;
}
