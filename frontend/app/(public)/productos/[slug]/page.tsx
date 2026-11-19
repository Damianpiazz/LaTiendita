import type { Metadata } from "next";

import { ProductDetailPage } from "@/features/product/pages/product-detail-page";
import {
  findProductBySlug,
  getProducts,
} from "@/features/product/services/catalog.service";

type ProductPageProps = {
  params: Promise<{ slug: string }>;
};

/** Prerenders every seeded product, so mock mode builds static pages. */
export async function generateStaticParams() {
  const products = await getProducts();

  return products.map((product) => ({ slug: product.slug }));
}

export async function generateMetadata({
  params,
}: ProductPageProps): Promise<Metadata> {
  const { slug } = await params;
  const product = await findProductBySlug(slug);

  return product ? { title: product.name } : {};
}

export default async function Page({ params }: ProductPageProps) {
  const { slug } = await params;

  return <ProductDetailPage slug={slug} />;
}
