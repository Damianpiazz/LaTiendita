import type { AdminClientSegment } from "@/features/admin/types";
import { cn } from "@/lib/utils";

const filters: { label: string; value: AdminClientSegment | undefined }[] = [
  { label: "Todos", value: undefined },
  { label: "Nuevos", value: "nueva" },
  { label: "Recurrentes", value: "recurrente" },
  { label: "Sin compras", value: "sin-compras" },
];

/** Search, segment filters and sort control above the clients table. */
export function AdminClientsToolbar({
  search,
  segment,
  sort,
  onSearchChange,
  onSegmentChange,
  onSortChange,
}: {
  search: string;
  segment: AdminClientSegment | undefined;
  sort: string;
  onSearchChange: (value: string) => void;
  onSegmentChange: (value: AdminClientSegment | undefined) => void;
  onSortChange: () => void;
}) {
  const ascending = sort.endsWith(",asc");

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div className="flex flex-wrap items-center gap-2">
        <div
          role="search"
          className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[320px]"
        >
          <label htmlFor="admin-clients-search" className="sr-only">
            Buscar clientes
          </label>
          <input
            id="admin-clients-search"
            type="search"
            value={search}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="Buscar por nombre, email o teléfono..."
            className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
        </div>

        {filters.map((filter) => {
          const active = segment === filter.value;

          return (
            <button
              key={filter.label}
              type="button"
              onClick={() => onSegmentChange(filter.value)}
              aria-current={active ? "true" : undefined}
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

      <button
        type="button"
        onClick={onSortChange}
        className="text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
      >
        {`Ordenar: Última compra ${ascending ? "↑" : "↓"}`}
      </button>
    </div>
  );
}
