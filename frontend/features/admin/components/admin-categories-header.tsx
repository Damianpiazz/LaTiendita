import type { AdminCategories } from "@/features/admin/types";

/**
 * Page header of the categories screen: title and the store totals on the left,
 * `+ Nueva categoría` on the right.
 *
 * The creation form is out of scope, so the action is presentational — the
 * same criterion the summary screen uses for its period control.
 */
export function AdminCategoriesHeader({
  categories,
}: {
  categories: AdminCategories;
}) {
  const totalProducts = categories.items.reduce(
    (sum, category) => sum + category.productCount,
    0,
  );

  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Categorías</h1>
        <p className="text-sm text-wf-ink-muted">
          {categories.items.length} categorías · {totalProducts} productos en total
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <span className="flex h-10 items-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse">
          + Nueva categoría
        </span>
      </div>
    </header>
  );
}
