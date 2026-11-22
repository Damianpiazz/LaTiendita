/**
 * Toolbar of the categories screen: search and the A–Z / Z–A sort control.
 *
 * The frame draws no menu behind the sort, so it ships as a single button with
 * the same label and the same grey token, toggling only the backend order.
 */
export function AdminCategoriesToolbar({
  search,
  sort,
  onSearchChange,
  onSortChange,
}: {
  search: string;
  sort: string;
  onSearchChange: (value: string) => void;
  onSortChange: (value: string) => void;
}) {
  const ascending = sort !== "name,desc";
  const sortLabel = ascending ? "Ordenar: A–Z ↓" : "Ordenar: Z–A ↑";

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div
        role="search"
        className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[320px]"
      >
        <label htmlFor="admin-categories-search" className="sr-only">
          Buscar categoría
        </label>
        <input
          id="admin-categories-search"
          type="search"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Buscar categoría..."
          className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
        />
      </div>

      <button
        type="button"
        onClick={() => onSortChange(ascending ? "name,desc" : "name,asc")}
        className="text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
      >
        {sortLabel}
      </button>
    </div>
  );
}
