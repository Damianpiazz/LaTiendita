import type { Metadata } from "next";

import { CategoryPage } from "@/features/category/pages/category-page";
import {
  findCategoryBySlug,
  getCategories,
} from "@/features/product/services/catalog.service";

type CategoryRouteProps = {
  params: Promise<{ slug: string }>;
};

/** Prerenders every seeded category, so mock mode builds static pages. */
export async function generateStaticParams() {
  const categories = await getCategories();

  return categories.map((category) => ({ slug: category.slug }));
}

export async function generateMetadata({
  params,
}: CategoryRouteProps): Promise<Metadata> {
  const { slug } = await params;
  const category = await findCategoryBySlug(slug);

  return category ? { title: category.name } : {};
}

export default async function Page({ params }: CategoryRouteProps) {
  const { slug } = await params;

  return <CategoryPage slug={slug} />;
}
