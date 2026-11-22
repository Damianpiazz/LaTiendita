"use client";

import { useEffect, useId, useState } from "react";
import type { FormEvent } from "react";

import { getAdminProductImages } from "@/features/admin/actions/product-actions";
import type {
  AdminCategory,
  AdminProduct,
  AdminProductPayload,
} from "@/features/admin/types";

const MIN_GALLERY_SLOTS = 3;
const MAX_GALLERY_SLOTS = 10;

type AdminProductFormModalProps = {
  product?: AdminProduct;
  categories: AdminCategory[];
  onClose: () => void;
  onSave: (payload: AdminProductPayload) => void;
};

function rowId(product: AdminProduct): string {
  return product.productId ?? product.id;
}

function gallerySlots(images: string[] | undefined): string[] {
  const values = (images ?? []).filter((image) => image.trim().length > 0);
  const filled = values.slice(0, MAX_GALLERY_SLOTS);
  const empty = Math.max(0, MIN_GALLERY_SLOTS - filled.length);

  return [...filled, ...Array.from({ length: empty }, () => "")];
}

/** New/edit form of the design system: scrolling body, fixed footer. */
export function AdminProductFormModal({
  product,
  categories,
  onClose,
  onSave,
}: AdminProductFormModalProps) {
  const titleId = useId();
  const matchedCategory = categories.find(
    (category) => category.name === product?.category,
  );
  const [title, setTitle] = useState(product?.name ?? "");
  const [sku, setSku] = useState(product?.sku ?? "");
  const [category, setCategory] = useState(matchedCategory?.id ?? "");
  const [price, setPrice] = useState(String(product?.price ?? ""));
  const [stock, setStock] = useState(String(product?.stock ?? ""));
  const [description, setDescription] = useState(product?.description ?? "");
  const [images, setImages] = useState<string[]>(gallerySlots(product?.images));
  const [published, setPublished] = useState(product?.published ?? true);

  useEffect(() => {
    if (!product) {
      return;
    }

    let active = true;
    void getAdminProductImages(rowId(product))
      .then((detailImages) => {
        if (active) {
          setImages(gallerySlots(detailImages));
        }
      })
      .catch(() => {
        if (active) {
          setImages(gallerySlots(product.images));
        }
      });

    return () => {
      active = false;
    };
  }, [product]);

  function setImageAt(index: number, value: string) {
    setImages((current) =>
      current.map((image, position) => (position === index ? value : image)),
    );
  }

  function addImage() {
    setImages((current) =>
      current.length >= MAX_GALLERY_SLOTS ? current : [...current, ""],
    );
  }

  function removeImage(index: number) {
    setImages((current) =>
      current.length <= MIN_GALLERY_SLOTS
        ? current.map((image, position) => (position === index ? "" : image))
        : current.filter((_, position) => position !== index),
    );
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSave({
      ...(product ? { productId: rowId(product) } : {}),
      title,
      sku,
      ...(category ? { categoryId: category } : {}),
      price: Number(price) || 0,
      quantity: Number(stock) || 0,
      ...(description.trim() ? { description: description.trim() } : {}),
      images: images.map((image) => image.trim()).filter(Boolean),
      published,
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
                    {product ? "Editar producto" : "Nuevo producto"}
                  </h2>
                  <p className="text-sm text-wf-ink-muted">
                    {product
                      ? `${product.name} · ${product.sku}`
                      : "Completá los datos para agregarlo al catálogo."}
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
                <div className="flex flex-wrap items-end justify-between gap-2">
                  <div className="flex flex-col gap-1">
                    <span className="text-sm font-medium text-wf-ink">
                      Imágenes
                    </span>
                    <span className="text-xs text-wf-ink-muted">
                      Hasta 10 · la primera es la principal
                    </span>
                  </div>
                  <button
                    type="button"
                    aria-label="Agregar imagen"
                    disabled={images.length >= MAX_GALLERY_SLOTS}
                    onClick={addImage}
                    className="flex size-8 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary hover:bg-wf-canvas disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    +
                  </button>
                </div>

                <div className="flex flex-wrap gap-2">
                  {images.map((image, index) => (
                    <div key={index} className="relative flex size-20 flex-col gap-1">
                      <label
                        htmlFor={`product-image-${index}`}
                        className="sr-only"
                      >
                        Imagen {index + 1}
                      </label>
                      <input
                        id={`product-image-${index}`}
                        value={image}
                        onChange={(event) => setImageAt(index, event.target.value)}
                        placeholder={`[ ${index + 1} ]`}
                        className="size-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-1 text-center text-xs text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
                      />
                      {index === 0 ? (
                        <span className="absolute -top-2 left-1 rounded-full bg-wf-inverse px-1 text-[10px] font-medium text-wf-on-inverse">
                          Principal
                        </span>
                      ) : null}
                      <button
                        type="button"
                        aria-label={`Quitar imagen ${index + 1}`}
                        onClick={() => removeImage(index)}
                        className="absolute -right-1 -top-2 flex size-5 items-center justify-center rounded-full border border-wf-line-strong bg-wf-surface text-xs text-wf-ink-secondary"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-name"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Nombre
                  </label>
                  <input
                    id="product-name"
                    value={title}
                    onChange={(event) => setTitle(event.target.value)}
                    className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                  />
                </div>
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-sku"
                    className="text-sm font-medium text-wf-ink"
                  >
                    SKU
                  </label>
                  <input
                    id="product-sku"
                    value={sku}
                    onChange={(event) => setSku(event.target.value)}
                    className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <label
                  htmlFor="product-category"
                  className="text-sm font-medium text-wf-ink"
                >
                  Categoría
                </label>
                <select
                  id="product-category"
                  value={category}
                  onChange={(event) => setCategory(event.target.value)}
                  className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                >
                  <option value="">Seleccionar categoría</option>
                  {categories.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-price"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Precio
                  </label>
                  <input
                    id="product-price"
                    type="number"
                    value={price}
                    onChange={(event) => setPrice(event.target.value)}
                    className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                  />
                </div>
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-stock"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Stock
                  </label>
                  <input
                    id="product-stock"
                    type="number"
                    value={stock}
                    onChange={(event) => setStock(event.target.value)}
                    className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <label
                  htmlFor="product-description"
                  className="text-sm font-medium text-wf-ink"
                >
                  Descripción
                </label>
                <textarea
                  id="product-description"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                  className="h-24 rounded-wf-sm border border-wf-line-strong bg-wf-surface p-3 text-sm text-wf-ink outline-none"
                />
              </div>

              <div className="flex items-center gap-2">
                <input
                  id="product-published"
                  type="checkbox"
                  checked={published}
                  onChange={(event) => setPublished(event.target.checked)}
                  className="size-5 rounded-wf-sm border border-wf-line-strong"
                />
                <label
                  htmlFor="product-published"
                  className="text-sm text-wf-ink"
                >
                  Publicado
                </label>
              </div>
            </div>
          </div>

          <footer
            data-admin-modal-footer
            className="flex shrink-0 items-center justify-end gap-3 border-t border-wf-line px-8 py-4"
          >
            <button
              type="button"
              onClick={onClose}
              className="flex h-10 w-[120px] items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm font-medium text-wf-ink-secondary"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="flex h-10 w-[160px] items-center justify-center rounded-wf-sm bg-wf-inverse text-sm font-medium text-wf-on-inverse"
            >
              {product ? "Guardar cambios" : "Guardar producto"}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
}
