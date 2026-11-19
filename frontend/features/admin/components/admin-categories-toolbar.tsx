/**
 * Toolbar of the categories screen: search on the left and the presentational
 * sort label on the right.
 *
 * The frame draws no menu behind the sort, so it ships as text, the same
 * decision taken on the summary and catalog screens.
 */
export function AdminCategoriesToolbar() {
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
          placeholder="Buscar categoría..."
          className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
        />
      </div>

      <span className="text-sm font-medium text-wf-ink-secondary">
        Ordenar: A–Z ↓
      </span>
    </div>
  );
}
