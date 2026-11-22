"use client";

import { useId, useState, type FormEvent } from "react";

import type { AdminCategory, AdminCategoryPayload } from "@/features/admin/types";

type AdminCategoryFormModalProps = {
  category?: AdminCategory;
  onClose: () => void;
  onSave: (payload: AdminCategoryPayload) => void;
};

function slugify(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

/** New/edit form of the categories frames: scrolling body, fixed footer. */
export function AdminCategoryFormModal({
  category,
  onClose,
  onSave,
}: AdminCategoryFormModalProps) {
  const titleId = useId();
  const nameId = `${titleId}-name`;
  const slugId = `${titleId}-slug`;
  const descriptionId = `${titleId}-description`;
  const imageId = `${titleId}-image`;
  const activeId = `${titleId}-active`;

  const [name, setName] = useState(category?.name ?? "");
  const [slug, setSlug] = useState(category?.slug ?? "");
  const [slugEdited, setSlugEdited] = useState(Boolean(category));
  const [description, setDescription] = useState(category?.description ?? "");
  const [imageUrl, setImageUrl] = useState(category?.imageUrl ?? "");
  const [active, setActive] = useState(category?.active ?? true);

  const featured = Boolean(category?.featured);

  function handleNameChange(value: string) {
    setName(value);
    if (!slugEdited) {
      setSlug(slugify(value));
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSave({
      ...(category ? { categoryId: category.id } : {}),
      name: name.trim(),
      slug: slug.trim(),
      description: description.trim(),
      featured,
      active,
      imageUrl: imageUrl.trim() || null,
    });
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div aria-hidden className="absolute inset-0 bg-wf-inverse/45" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className="relative flex max-h-[calc(100vh-2rem)] w-full max-w-[640px] flex-col overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface"
      >
        <form onSubmit={handleSubmit} className="flex min-h-0 flex-1 flex-col">
          <div
            data-admin-modal-body
            className="min-h-0 flex-1 overflow-y-auto p-8"
          >
            <div className="flex flex-col gap-6">
              <div className="flex items-start justify-between gap-4">
                <div className="flex flex-col gap-1">
                  <h2 id={titleId} className="text-2xl font-bold text-wf-ink">
                    {category ? "Editar categoría" : "Nueva categoría"}
                  </h2>
                  <p className="text-sm text-wf-ink-muted">
                    {category
                      ? `Actualizá los datos de ${category.name}.`
                      : "Completá los datos para publicarla en la tienda."}
                  </p>
                </div>
                <button
                  type="button"
                  aria-label="Cerrar"
                  onClick={onClose}
                  className="flex size-8 items-center justify-center text-lg text-wf-ink-muted hover:text-wf-ink"
                >
                  ×
                </button>
              </div>

              <div className="flex flex-col gap-3">
                <span className="text-sm font-medium text-wf-ink">Imagen</span>
                <div className="flex h-[124px] items-center justify-center rounded-wf-md bg-wf-subtle text-xs text-wf-ink-subtle">
                  [ Imagen ]
                </div>
                <label htmlFor={imageId} className="text-sm font-medium text-wf-ink">
                  URL de imagen
                </label>
                <input
                  id={imageId}
                  value={imageUrl}
                  onChange={(event) => setImageUrl(event.target.value)}
                  placeholder="Ej. /images/remeras.jpg"
                  className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                />
                <div className="flex flex-col gap-0.5">
                  <p className="text-xs text-wf-ink-muted">
                    {category
                      ? "Imagen actual de la categoría."
                      : "Subí una imagen para la categoría."}
                  </p>
                  <p className="text-xs text-wf-ink-subtle">
                    PNG o JPG · recomendado 400 × 400
                  </p>
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor={nameId} className="text-sm font-medium text-wf-ink">
                  Nombre *
                </label>
                <input
                  id={nameId}
                  value={name}
                  onChange={(event) => handleNameChange(event.target.value)}
                  placeholder="Ej. Remeras"
                  className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                />
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor={slugId} className="text-sm font-medium text-wf-ink">
                  Slug *
                </label>
                <input
                  id={slugId}
                  value={slug}
                  onChange={(event) => {
                    setSlugEdited(true);
                    setSlug(slugify(event.target.value));
                  }}
                  placeholder="/categorias/remeras"
                  className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                />
                <p className="text-xs text-wf-ink-muted">
                  {category
                    ? "El slug se mantiene para no romper links existentes."
                    : "Se genera desde el nombre. Podés editarlo."}
                </p>
              </div>

              <div className="flex flex-col gap-2">
                <label
                  htmlFor={descriptionId}
                  className="text-sm font-medium text-wf-ink"
                >
                  Descripción
                </label>
                <textarea
                  id={descriptionId}
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                  placeholder="Contá qué productos incluye..."
                  className="h-24 rounded-wf-sm border border-wf-line-strong bg-wf-surface p-3 text-sm text-wf-ink outline-none"
                />
              </div>

              <div className="flex items-center gap-2">
                <input
                  id={activeId}
                  type="checkbox"
                  checked={active}
                  onChange={(event) => setActive(event.target.checked)}
                  className="size-5 rounded-wf-sm border border-wf-line-strong"
                />
                <label htmlFor={activeId} className="text-sm text-wf-ink">
                  Categoría activa · visible en la tienda
                </label>
              </div>
            </div>
          </div>

          <footer className="flex shrink-0 items-center justify-end gap-3 border-t border-wf-line px-8 py-4">
            <button
              type="button"
              onClick={onClose}
              className="flex h-10 w-[120px] items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="flex h-10 min-w-[160px] items-center justify-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse"
            >
              {category ? "Guardar cambios" : "Crear categoría"}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
}
