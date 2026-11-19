import { ProductCard } from "@/features/product/components/product-card";
import { SectionHeader } from "@/features/product/components/section-header";
import type { Product } from "@/features/product/types";

/** Related grid of the detail screen: four compact cards on the page canvas. */
export function RelatedProducts({ products }: { products: Product[] }) {
  return (
    <section className="px-12 py-12">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-6">
        <SectionHeader
          title="Productos relacionados"
          actionLabel="Ver todo"
          actionHref="/productos"
        />
        <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {products.map((product) => (
            <li key={product.id} className="flex">
              <ProductCard product={product} variant="compact" />
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
