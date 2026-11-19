import { cn } from "@/lib/utils";

const filters = ["Todos", "Nuevos", "Recurrentes", "Sin compras"];

/**
 * Search, segment filters and sort control above the clients table.
 *
 * `Todos` is the drawn active filter and nothing is clickable yet: the frame
 * only draws the resting state of the four segments, and the mock always
 * serves the first page.
 */
export function AdminClientsToolbar() {
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
            placeholder="Buscar por nombre, email o teléfono..."
            className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
        </div>

        {filters.map((filter) => {
          const active = filter === "Todos";

          return (
            <span
              key={filter}
              aria-current={active ? "true" : undefined}
              className={cn(
                "flex h-10 items-center rounded-full px-4 text-sm",
                active
                  ? "bg-wf-inverse font-medium text-wf-on-inverse"
                  : "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
              )}
            >
              {filter}
            </span>
          );
        })}
      </div>

      <span className="text-sm font-medium text-wf-ink-secondary">
        Ordenar: Última compra ↓
      </span>
    </div>
  );
}
