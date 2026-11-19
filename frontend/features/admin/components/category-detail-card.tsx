import Link from "next/link";

import type { AdminCategory } from "@/features/admin/types";

/**
 * Detail card of the selected category, 412 wide in the frame.
 *
 * `Editar` and `Eliminar` are presentational because no form exists yet; the
 * store link is real and opens the public category page.
 */
export function CategoryDetailCard({
  category,
}: {
  category: AdminCategory;
}) {
  return (
    <aside className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5 xl:w-[412px] xl:shrink-0">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-base font-bold text-wf-ink">
          Detalle de la categoría
        </h2>
        <span aria-hidden className="font-bold text-wf-ink-placeholder">
          ···
        </span>
      </div>

      <div className="flex h-[124px] items-center justify-center rounded-wf-md bg-wf-subtle text-xs text-wf-ink-subtle">
        [ Imagen ]
      </div>

      <div className="flex flex-col gap-1">
        <h3 className="text-lg font-bold text-wf-ink">{category.name}</h3>
        <p className="text-xs text-wf-ink-muted">/categorias/{category.slug}</p>
      </div>

      <p className="text-sm text-wf-ink-secondary">{category.description}</p>

      <div className="h-px bg-wf-line" />

      <div className="flex items-start justify-between gap-4">
        <div className="flex flex-col gap-1">
          <span className="text-lg font-bold text-wf-ink">
            {category.productCount}
          </span>
          <span className="text-xs text-wf-ink-muted">productos</span>
        </div>
        <div className="flex flex-col items-end gap-1">
          <span className="text-lg font-bold text-wf-ink">
            {category.lowStockCount}
          </span>
          <span className="text-xs text-wf-ink-muted">con stock bajo</span>
        </div>
      </div>

      <div className="h-px bg-wf-line" />

      <div className="flex flex-col gap-2">
        <span className="flex h-10 items-center justify-center rounded-wf-sm bg-wf-inverse text-sm font-medium text-wf-on-inverse">
          Editar categoría
        </span>
        <Link
          href={`/categorias/${category.slug}`}
          className="flex h-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          Ver en tienda ↗
        </Link>
      </div>

      <span className="flex h-8 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-xs font-medium text-wf-ink-muted">
        Eliminar categoría
      </span>
    </aside>
  );
}
