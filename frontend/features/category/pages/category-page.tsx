import { notFound } from "next/navigation";

import { BenefitsBar } from "@/components/layout/benefits-bar";
import { CategoryProducts } from "@/features/category/components/category-products";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import {
  findCategoryBySlug,
  getCategoryProducts,
} from "@/features/product/services/catalog.service";

/**
 * `Category Tecnologia Wireframe` of the design file, generic for any category.
 *
 * The category arrives through the cached service, so an unknown slug can be
 * answered with `notFound()` and the grid follows the same mock (or real) API
 * as the rest of the store. Filtering and pagination are client state inside
 * `CategoryProducts`, which keeps the route statically prerendered.
 */
export async function CategoryPage({ slug }: { slug: string }) {
  const category = await findCategoryBySlug(slug);

  if (!category) {
    notFound();
  }

  const products = await getCategoryProducts(category.id);

  return (
    <>
      <Breadcrumbs
        size="md"
        items={[
          { label: "Inicio", href: "/" },
          { label: "Categorías", href: "/categorias" },
          { label: category.name },
        ]}
      />

      <section className="px-12 py-10">
        <div className="mx-auto w-full max-w-[1440px]">
          <CategoryProducts category={category} products={products} />
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
