import Link from "next/link";

import type { AdminCategory } from "@/features/admin/types";
import { cn } from "@/lib/utils";

/**
 * Categories card: header, six 72px rows and the footer range.
 *
 * Every row draws the store path and its product count; the selected one is
 * filled with `background/canvas`, exactly as the frame highlights `Remeras`.
 * The footer repeats the list length, so the copy follows the seed as well as
 * the real page, and each row selects the category shown in the detail card.
 */
export function CategoryList({
  items,
  selectedId,
  updatedLabel,
  onSelect,
}: {
  items: AdminCategory[];
  selectedId: string;
  updatedLabel: string;
  onSelect: (category: AdminCategory) => void;
}) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="flex flex-wrap items-center justify-between gap-3 p-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">Todas las categorías</h2>
          <p className="text-xs text-wf-ink-muted">
            {items.length} categorías activas en la tienda
          </p>
        </div>
        <Link
          href="/categorias"
          className="text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          Ver en tienda ↗
        </Link>
      </div>

      <ul className="flex flex-col">
        {items.map((category) => {
          const selected = category.id === selectedId;

          return (
            <li
              key={category.id}
              className={cn(
                "flex h-[72px] items-center border-b border-wf-line last:border-b-0",
                selected && "bg-wf-canvas",
              )}
            >
              <button
                type="button"
                aria-current={selected ? "true" : undefined}
                onClick={() => onSelect(category)}
                className="flex h-full w-full items-center gap-3 px-5 text-left"
              >
                <span
                  aria-hidden
                  className={cn(
                    "size-10 shrink-0 rounded-wf-sm",
                    selected ? "bg-wf-muted" : "bg-wf-subtle",
                  )}
                />

                <span className="flex min-w-0 flex-1 flex-col gap-1">
                  <span className="truncate text-sm font-bold text-wf-ink">
                    {category.name}
                  </span>
                  <span className="truncate text-xs text-wf-ink-muted">
                    /categorias/{category.slug}
                  </span>
                </span>

                <span className="shrink-0 text-xs text-wf-ink-muted">
                  {category.productCount} productos
                </span>
                <span aria-hidden className="text-wf-ink-placeholder">
                  ›
                </span>
              </button>
            </li>
          );
        })}
      </ul>

      <div className="flex h-10 flex-wrap items-center justify-between gap-3 border-t border-wf-line px-5 text-xs text-wf-ink-muted">
        <span>
          Mostrando {items.length} de {items.length} categorías
        </span>
        <span>Última actualización: {updatedLabel}</span>
      </div>
    </section>
  );
}
