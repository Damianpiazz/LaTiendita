import { BenefitsBar } from "@/components/layout/benefits-bar";
import { CategoryCatalog } from "@/features/category/components/category-catalog";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { getCategories } from "@/features/product/services/catalog.service";

/**
 * `Categories Wireframe` of the design file.
 *
 * The catalog arrives through the cached service, so the grid follows the same
 * mock (or real) API as the rest of the store; the filter strip is client state
 * inside `CategoryCatalog`.
 */
export async function CategoriesPage() {
  const categories = await getCategories();

  return (
    <>
      <Breadcrumbs
        size="md"
        items={[{ label: "Inicio", href: "/" }, { label: "Categorías" }]}
      />

      <section className="px-12 py-10">
        <div className="mx-auto w-full max-w-[1440px]">
          <CategoryCatalog categories={categories} />
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
