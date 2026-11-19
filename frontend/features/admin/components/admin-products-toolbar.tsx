import { cn } from "@/lib/utils";

const filters = ["Todos", "Publicados", "Borradores", "Sin stock"];

/**
 * Toolbar of the products screen: search, the state filters and the
 * presentational category/sort selectors.
 *
 * `Todos` is the drawn active filter; the other three are outline pills. None
 * filters the table yet — the mock always serves the same page.
 */
export function AdminProductsToolbar() {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div className="flex flex-wrap items-center gap-2">
        <div
          role="search"
          className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[320px]"
        >
          <label htmlFor="admin-products-search" className="sr-only">
            Buscar productos
          </label>
          <input
            id="admin-products-search"
            type="search"
            placeholder="Buscar por nombre o SKU..."
            className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
        </div>

        {filters.map((filter, index) => (
          <span
            key={filter}
            aria-current={index === 0 ? "true" : undefined}
            className={cn(
              "flex h-10 items-center rounded-full px-4 text-sm",
              index === 0
                ? "bg-wf-inverse font-medium text-wf-on-inverse"
                : "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
            )}
          >
            {filter}
          </span>
        ))}
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <span className="flex items-center gap-2 text-sm">
          <span className="text-wf-ink-muted">Categoría:</span>
          <span className="font-medium text-wf-ink">Todas</span>
          <span aria-hidden className="text-wf-ink-placeholder">
            ↓
          </span>
        </span>
        <span className="text-sm font-medium text-wf-ink-secondary">
          Ordenar: Recientes ↓
        </span>
      </div>
    </div>
  );
}
