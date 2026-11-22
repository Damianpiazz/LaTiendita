import Link from "next/link";

import { setCategoryFeatured } from "@/features/admin/actions/category-actions";
import type { AdminCategory } from "@/features/admin/types";

/**
 * Detail card of the selected category, 412 wide in the frame.
 *
 * The featured control still persists through the catalog update endpoint;
 * edit, delete and the active checkbox now call the workspace actions.
 */
export function CategoryDetailCard({
  category,
  onEdit,
  onDelete,
  onActiveChange,
}: {
  category: AdminCategory;
  onEdit: () => void;
  onDelete: () => void;
  onActiveChange: (active: boolean) => void;
}) {
  const activeId = `category-active-${category.id}`;
  const imageUrl = category.imageUrl?.trim() ?? "";

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

      {imageUrl ? (
        // The source is an arbitrary admin URL, so the plain img keeps it exact.
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={imageUrl}
          alt={category.name}
          className="h-[124px] w-full rounded-wf-md object-cover"
        />
      ) : (
        <div className="flex h-[124px] items-center justify-center rounded-wf-md bg-wf-subtle text-xs text-wf-ink-subtle">
          [ Imagen ]
        </div>
      )}

      <div className="flex flex-col gap-1">
        <h3 className="text-lg font-bold text-wf-ink">{category.name}</h3>
        <p className="text-xs text-wf-ink-muted">/categorias/{category.slug}</p>
      </div>

      <p className="text-sm text-wf-ink-secondary">{category.description}</p>

      <div className="flex items-center gap-2">
        <input
          id={activeId}
          type="checkbox"
          checked={category.active ?? true}
          onChange={(event) => onActiveChange(event.target.checked)}
          className="size-5 rounded-wf-sm border border-wf-line-strong"
        />
        <label htmlFor={activeId} className="text-sm text-wf-ink">
          Categoría activa
        </label>
      </div>

      <form action={setCategoryFeatured}>
        <input type="hidden" name="categoryId" value={category.id} />
        <input
          type="hidden"
          name="featured"
          value={category.featured ? "false" : "true"}
        />
        <button
          type="submit"
          className="flex h-10 w-full items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          {category.featured ? "Quitar de destacadas" : "Marcar como destacada"}
        </button>
      </form>

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
        <button
          type="button"
          onClick={onEdit}
          className="flex h-10 items-center justify-center rounded-wf-sm bg-wf-inverse text-sm font-medium text-wf-on-inverse transition-opacity hover:opacity-90"
        >
          Editar categoría
        </button>
        <Link
          href={`/categorias/${category.slug}`}
          className="flex h-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          Ver en tienda ↗
        </Link>
      </div>

      <button
        type="button"
        onClick={onDelete}
        className="flex h-8 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-xs font-medium text-wf-ink-muted transition-colors hover:text-wf-ink"
      >
        Eliminar categoría
      </button>
    </aside>
  );
}
