"use client";

import { useEffect, useId, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";

import {
  getAdminProductDetail,
  uploadAdminProductImage,
} from "@/features/admin/actions/product-actions";
import type {
  AdminCategory,
  AdminProduct,
  AdminProductPayload,
  AdminProductSpec,
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

function textLines(values: string[] | string | undefined | null): string[] {
  const raw = Array.isArray(values) ? values : values ? [values] : [];
  return raw.flatMap((value) => value.split(/\r?\n/)).filter((value) => value.trim());
}

function specLines(specs: AdminProductSpec[] | undefined | null): string {
  return (specs ?? [])
    .map((spec) => `${spec.label}: ${spec.value}`)
    .join("\n");
}

function parseSpecs(value: string): AdminProductSpec[] {
  return value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .flatMap((line) => {
      const separator = line.includes(":") ? ":" : "=";
      const index = line.indexOf(separator);
      if (index <= 0) return [];
      return [
        {
          label: line.slice(0, index).trim(),
          value: line.slice(index + 1).trim(),
        },
      ];
    })
    .filter((spec) => spec.label.length > 0 && spec.value.length > 0);
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
  const [slug, setSlug] = useState("");
  const [sku, setSku] = useState(product?.sku ?? "");
  const [subcategory, setSubcategory] = useState("");
  const [featured, setFeatured] = useState(false);
  const [category, setCategory] = useState(matchedCategory?.id ?? "");
  const [price, setPrice] = useState(String(product?.price ?? ""));
  const [previousPrice, setPreviousPrice] = useState("");
  const [fulfillment, setFulfillment] = useState("");
  const [stock, setStock] = useState(String(product?.stock ?? ""));
  const [description, setDescription] = useState(product?.description ?? "");
  const [highlights, setHighlights] = useState("");
  const [specs, setSpecs] = useState("");
  const [images, setImages] = useState<string[]>(gallerySlots(product?.images));
  const [published, setPublished] = useState(product?.published ?? true);
  const [uploadingIndex, setUploadingIndex] = useState<number | null>(null);
  const [uploadError, setUploadError] = useState("");

  useEffect(() => {
    if (!product) {
      return;
    }

    let active = true;
    void getAdminProductDetail(rowId(product))
      .then((detail) => {
        if (!active) {
          return;
        }
        setImages(gallerySlots(detail.images));
        setSlug(detail.slug ?? "");
        setSubcategory(detail.subcategory ?? "");
        setFeatured(Boolean(detail.featured));
        setPreviousPrice(
          detail.originalPrice === null || detail.originalPrice === undefined
            ? ""
            : String(detail.originalPrice),
        );
        setFulfillment(detail.fulfillment ?? "");
        setDescription(textLines(detail.description).join("\n") || product.description || "");
        setHighlights(textLines(detail.highlights).join("\n"));
        setSpecs(specLines(detail.specs));
        if (detail.published !== null && detail.published !== undefined) {
          setPublished(detail.published);
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

  async function uploadImage(index: number, event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) {
      return;
    }

    setUploadError("");
    setUploadingIndex(index);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const uploaded = await uploadAdminProductImage(formData);
      setImageAt(index, uploaded.url);
    } catch (error) {
      setUploadError(
        error instanceof Error
          ? error.message
          : "No se pudo subir la imagen. Intentá de nuevo.",
      );
    } finally {
      setUploadingIndex(null);
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsedSpecs = parseSpecs(specs);
    const parsedHighlights = textLines(highlights).map((line) => line.trim());
    onSave({
      ...(product ? { productId: rowId(product) } : {}),
      title,
      sku,
      ...(category ? { categoryId: category } : {}),
      price: Number(price) || 0,
      quantity: Number(stock) || 0,
      ...(description.trim() ? { description: description.trim() } : {}),
      ...(slug.trim() ? { slug: slug.trim() } : {}),
      ...(subcategory.trim() ? { subcategory: subcategory.trim() } : {}),
      featured,
      ...(previousPrice.trim() ? { originalPrice: Number(previousPrice) } : {}),
      ...(fulfillment.trim() ? { fulfillment: fulfillment.trim() } : {}),
      ...(parsedHighlights.length > 0 ? { highlights: parsedHighlights } : {}),
      specs: parsedSpecs,
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
                    <div key={index} className="relative flex w-20 flex-col gap-1">
                      <div className="relative size-20 overflow-hidden rounded-wf-sm border border-wf-line-strong bg-wf-canvas">
                        {image.trim() ? (
                          <div
                            aria-hidden
                            className="size-full bg-cover bg-center"
                            style={{
                              backgroundImage: `url("${image.replace(/"/g, "%22")}")`,
                            }}
                          />
                        ) : (
                          <span className="flex size-full items-center justify-center text-xs text-wf-ink-placeholder">
                            [ {index + 1} ]
                          </span>
                        )}
                      </div>
                      {index === 0 ? (
                        <span className="absolute -top-2 left-1 rounded-full bg-wf-inverse px-1 text-[10px] font-medium text-wf-on-inverse">
                          Principal
                        </span>
                      ) : null}
                      <label htmlFor={`product-image-${index}`} className="sr-only">
                        Imagen {index + 1}
                      </label>
                      <input
                        id={`product-image-${index}`}
                        value={image}
                        onChange={(event) => setImageAt(index, event.target.value)}
                        placeholder={`[ ${index + 1} ]`}
                        className="h-8 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-1 text-center text-xs text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
                      />
                      <label
                        htmlFor={`product-image-file-${index}`}
                        className="cursor-pointer text-center text-[10px] text-wf-ink-secondary underline"
                      >
                        {uploadingIndex === index ? "Subiendo…" : "Subir archivo"}
                      </label>
                      <input
                        id={`product-image-file-${index}`}
                        type="file"
                        accept="image/*"
                        disabled={uploadingIndex !== null}
                        onChange={(event) => void uploadImage(index, event)}
                        className="sr-only"
                      />
                      <button
                        type="button"
                        aria-label={`Quitar imagen ${index + 1}`}
                        onClick={() => removeImage(index)}
                        className="absolute -right-1 top-7 flex size-5 items-center justify-center rounded-full border border-wf-line-strong bg-wf-surface text-xs text-wf-ink-secondary"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
                {uploadError ? (
                  <p role="alert" className="text-xs font-medium text-wf-ink">
                    {uploadError}
                  </p>
                ) : null}
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

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-slug"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Slug
                  </label>
                  <input
                    id="product-slug"
                    value={slug}
                    onChange={(event) => setSlug(event.target.value)}
                    className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                  />
                </div>
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-subcategory"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Subcategoría
                  </label>
                  <input
                    id="product-subcategory"
                    value={subcategory}
                    onChange={(event) => setSubcategory(event.target.value)}
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

              <div className="grid gap-4 sm:grid-cols-3">
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
                    htmlFor="product-previous-price"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Valor anterior
                  </label>
                  <input
                    id="product-previous-price"
                    type="number"
                    value={previousPrice}
                    onChange={(event) => setPreviousPrice(event.target.value)}
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
                  htmlFor="product-fulfillment"
                  className="text-sm font-medium text-wf-ink"
                >
                  Plazo de entrega
                </label>
                <input
                  id="product-fulfillment"
                  value={fulfillment}
                  onChange={(event) => setFulfillment(event.target.value)}
                  placeholder="Ej. 24/48 hs"
                  className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none"
                />
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

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-highlights"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Puntos destacados
                  </label>
                  <textarea
                    id="product-highlights"
                    value={highlights}
                    onChange={(event) => setHighlights(event.target.value)}
                    placeholder="Uno por línea"
                    className="h-24 rounded-wf-sm border border-wf-line-strong bg-wf-surface p-3 text-sm text-wf-ink outline-none"
                  />
                </div>
                <div className="flex flex-col gap-2">
                  <label
                    htmlFor="product-specs"
                    className="text-sm font-medium text-wf-ink"
                  >
                    Especificaciones
                  </label>
                  <textarea
                    id="product-specs"
                    value={specs}
                    onChange={(event) => setSpecs(event.target.value)}
                    placeholder="Etiqueta: valor, uno por línea"
                    className="h-24 rounded-wf-sm border border-wf-line-strong bg-wf-surface p-3 text-sm text-wf-ink outline-none"
                  />
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-6">
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
                <div className="flex items-center gap-2">
                  <input
                    id="product-featured"
                    type="checkbox"
                    checked={featured}
                    onChange={(event) => setFeatured(event.target.checked)}
                    className="size-5 rounded-wf-sm border border-wf-line-strong"
                  />
                  <label
                    htmlFor="product-featured"
                    className="text-sm text-wf-ink"
                  >
                    Destacado
                  </label>
                </div>
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
              disabled={uploadingIndex !== null}
              className="flex h-10 w-[160px] items-center justify-center rounded-wf-sm bg-wf-inverse text-sm font-medium text-wf-on-inverse disabled:opacity-60"
            >
              {product ? "Guardar cambios" : "Guardar producto"}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
}
