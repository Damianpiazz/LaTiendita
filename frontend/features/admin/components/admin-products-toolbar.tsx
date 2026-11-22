import type { AdminCategory, AdminProductStatusFilter } from "@/features/admin/types";
import { cn } from "@/lib/utils";

const statusFilters: { label: string; value: AdminProductStatusFilter }[] = [
  { label: "Todos", value: "all" },
  { label: "Publicados", value: "publicado" },
  { label: "Borradores", value: "borrador" },
  { label: "Sin stock", value: "agotado" },
];

const sortOptions = [
  { label: "Recientes ↓", value: "id,desc" },
  { label: "Nombre ↑", value: "title,asc" },
  { label: "Nombre ↓", value: "title,desc" },
  { label: "Precio ↑", value: "price,asc" },
  { label: "Precio ↓", value: "price,desc" },
];

type AdminProductsToolbarProps = {
  categories: AdminCategory[];
  search: string;
  status: AdminProductStatusFilter;
  categoryId: string;
  sort: string;
  onSearchChange: (value: string) => void;
  onStatusChange: (value: AdminProductStatusFilter) => void;
  onCategoryChange: (value: string) => void;
  onSortChange: (value: string) => void;
};

/** Search, backend status pills and the category/sort selects. */
export function AdminProductsToolbar({
  categories,
  search,
  status,
  categoryId,
  sort,
  onSearchChange,
  onStatusChange,
  onCategoryChange,
  onSortChange,
}: AdminProductsToolbarProps) {
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
            value={search}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="Buscar por nombre o SKU..."
            className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
        </div>

        {statusFilters.map((filter) => {
          const active = filter.value === status;

          return (
            <button
              key={filter.value}
              type="button"
              aria-current={active ? "true" : undefined}
              onClick={() => onStatusChange(filter.value)}
              className={cn(
                "flex h-10 items-center rounded-full px-4 text-sm transition-colors",
                active
                  ? "bg-wf-inverse font-medium text-wf-on-inverse"
                  : "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
              )}
            >
              {filter.label}
            </button>
          );
        })}
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <div className="flex items-center gap-2 text-sm">
          <label htmlFor="admin-products-category" className="text-wf-ink-muted">
            Categoría
          </label>
          <select
            id="admin-products-category"
            value={categoryId}
            onChange={(event) => onCategoryChange(event.target.value)}
            className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-2 text-sm font-medium text-wf-ink"
          >
            <option value="">Todas</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 text-sm">
          <label htmlFor="admin-products-sort" className="text-wf-ink-muted">
            Ordenar
          </label>
          <select
            id="admin-products-sort"
            value={sort}
            onChange={(event) => onSortChange(event.target.value)}
            className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-2 text-sm font-medium text-wf-ink-secondary"
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}
