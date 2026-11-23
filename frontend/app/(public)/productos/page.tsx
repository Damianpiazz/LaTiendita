import type { Metadata } from "next";

import { BenefitsBar } from "@/components/layout/benefits-bar";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { ProductsCatalog } from "@/features/product/components/products-catalog";
import { getCategories, getProducts } from "@/features/product/services/catalog.service";

export const metadata: Metadata = {
  title: "Productos",
};

type PageProps = {
  searchParams: Promise<{ q?: string | string[] }>;
};

export default async function ProductsPage({ searchParams }: PageProps) {
  const params = await searchParams;
  const rawQuery = Array.isArray(params.q) ? params.q[0] : params.q;
  const [products, categories] = await Promise.all([getProducts({ size: 100 }), getCategories()]);

  return (
    <>
      <Breadcrumbs
        size="md"
        items={[{ label: "Inicio", href: "/" }, { label: "Productos" }]}
      />

      <section className="px-12 py-10">
        <div className="mx-auto w-full max-w-[1440px]">
          <ProductsCatalog
            products={products}
            categories={categories}
            initialQuery={rawQuery ?? ""}
          />
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
