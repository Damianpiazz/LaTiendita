import { BenefitsBar } from "@/components/layout/benefits-bar";
import { NewsletterSection } from "@/components/layout/newsletter-section";
import { CategoryCard } from "@/features/home/components/category-card";
import { HeroSection } from "@/features/home/components/hero-section";
import { ProductCard } from "@/features/product/components/product-card";
import { SectionHeader } from "@/features/product/components/section-header";
import {
  getCategories,
  getFeaturedProducts,
} from "@/features/product/services/catalog.service";

/**
 * `Home Wireframe` of the design file.
 *
 * Data arrives through the catalog service, so the same screen works against
 * the mock adapter and against the real API.
 */
export async function HomePage() {
  const [categories, products] = await Promise.all([
    getCategories({ featured: true }),
    getFeaturedProducts(),
  ]);

  return (
    <>
      <HeroSection />

      <section id="categorias" className="px-12 py-16">
        <div className="mx-auto max-w-[1440px]">
          <SectionHeader
            title="Comprá por categoría"
            actionLabel="Ver todas"
            actionHref="/categorias"
          />
          <ul className="mt-8 grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-5">
            {categories.map((category) => (
              <li key={category.id}>
                <CategoryCard category={category} />
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section id="productos" className="px-12 py-16">
        <div className="mx-auto max-w-[1440px]">
          <SectionHeader
            title="Productos destacados"
            actionLabel="Ver todo"
            actionHref="/productos"
          />
          <ul className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {products.map((product) => (
              <li key={product.id} className="flex">
                <ProductCard product={product} />
              </li>
            ))}
          </ul>
        </div>
      </section>

      <BenefitsBar />
      <NewsletterSection />
    </>
  );
}
