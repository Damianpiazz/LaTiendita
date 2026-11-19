import type { AdminProducts } from "@/features/admin/types";

/**
 * Page header of the products screen: title, the three store totals and the
 * export/creation actions.
 *
 * Neither action has a flow behind it in the frame, so both are
 * presentational, same criterion as the categories screen.
 */
export function AdminProductsHeader({ products }: { products: AdminProducts }) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Productos</h1>
        <p className="text-sm text-wf-ink-muted">
          {products.totalProducts} productos · {products.totalCategories} categorías
          · {products.totalLowStock} con stock bajo
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <span className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary">
          Exportar CSV
        </span>
        <span className="flex h-10 items-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse">
          + Nuevo producto
        </span>
      </div>
    </header>
  );
}
