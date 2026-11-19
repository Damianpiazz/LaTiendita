import { notFound } from "next/navigation";

import { BenefitsBar } from "@/components/layout/benefits-bar";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { ProductDetails } from "@/features/product/components/product-details";
import { ProductGallery } from "@/features/product/components/product-gallery";
import { ProductInfo } from "@/features/product/components/product-info";
import { RelatedProducts } from "@/features/product/components/related-products";
import {
  findProductBySlug,
  getRelatedProducts,
} from "@/features/product/services/catalog.service";
import { ReviewsSection } from "@/features/review/components/reviews-section";
import {
  getReviewSummary,
  getReviews,
} from "@/features/review/services/review.service";

/**
 * `Product Detail Wireframe` of the design file.
 *
 * Data arrives through the catalog and review services, so the screen works
 * against the mock adapter and against the real APIs with no changes.
 */
export async function ProductDetailPage({ slug }: { slug: string }) {
  const product = await findProductBySlug(slug);

  if (!product) {
    notFound();
  }

  const [summary, reviewPage, related] = await Promise.all([
    getReviewSummary(product.id),
    getReviews(product.id, { page: 1, size: 2 }),
    getRelatedProducts(4),
  ]);

  return (
    <>
      <Breadcrumbs
        items={[
          { label: "Inicio", href: "/" },
          { label: "Categorías", href: "/categorias" },
          { label: product.categoryName, href: `/categorias/${product.categoryId}` },
          { label: product.name },
        ]}
      />

      <section className="bg-wf-surface px-12 py-10">
        <div className="mx-auto grid w-full max-w-[1440px] gap-14 lg:grid-cols-[640px_minmax(0,1fr)]">
          <ProductGallery count={product.galleryCount} />
          <ProductInfo product={product} />
        </div>
      </section>

      <ProductDetails product={product} />

      <ReviewsSection
        product={product}
        summary={summary}
        reviews={reviewPage.items}
      />

      <RelatedProducts products={related} />
      <BenefitsBar />
    </>
  );
}
