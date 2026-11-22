"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import {
  createAdminProduct,
  deleteAdminProducts,
  exportAdminProducts,
  listAdminProducts,
  publishAdminProducts,
  updateAdminProduct,
} from "@/features/admin/actions/product-actions";
import { AdminProductConfirmDialog } from "@/features/admin/components/admin-product-confirm-dialog";
import { AdminProductFormModal } from "@/features/admin/components/admin-product-form-modal";
import { AdminPagination } from "@/features/admin/components/admin-pagination";
import { AdminProductsBulkBar } from "@/features/admin/components/admin-products-bulk-bar";
import { AdminProductsHeader } from "@/features/admin/components/admin-products-header";
import { AdminProductsToolbar } from "@/features/admin/components/admin-products-toolbar";
import { ProductStatusBadge } from "@/features/admin/components/product-status-badge";
import type {
  AdminCategory,
  AdminProduct,
  AdminProductPayload,
  AdminProducts,
  AdminProductStatusFilter,
  AdminProductsQuery,
} from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";
import { cn } from "@/lib/utils";

const PAGE_SIZE = 8;
const DEFAULT_SORT = "id,desc";

type FormState = { open: false } | { open: true; product?: AdminProduct };
type ConfirmState = { kind: "archive" | "delete"; ids: string[] } | null;

type AdminProductsWorkspaceProps = {
  initialProducts: AdminProducts;
  categories: AdminCategory[];
};

function rowId(product: AdminProduct): string {
  return product.productId ?? product.id;
}

/**
 * Interactive products screen. The server seeds the first page; filters,
 * mutations and the gallery forms go through the same server actions.
 */
export function AdminProductsWorkspace({
  initialProducts,
  categories,
}: AdminProductsWorkspaceProps) {
  const [products, setProducts] = useState(initialProducts);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<AdminProductStatusFilter>("all");
  const [categoryId, setCategoryId] = useState("");
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(
    () =>
      new Set(
        initialProducts.items
          .filter((product) => product.selected)
          .map(rowId),
      ),
  );
  const [formState, setFormState] = useState<FormState>({ open: false });
  const [confirmState, setConfirmState] = useState<ConfirmState>(null);
  const [exporting, setExporting] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  const skipInitialFetch = useRef(true);
  const requestId = useRef(0);

  const query = useMemo<AdminProductsQuery>(() => {
    const trimmed = search.trim();

    return {
      ...(trimmed ? { q: trimmed } : {}),
      ...(status !== "all" ? { status } : {}),
      ...(categoryId ? { categoryId } : {}),
      page,
      size: products.pageSize || PAGE_SIZE,
      sort,
    };
  }, [categoryId, page, products.pageSize, search, sort, status]);

  useEffect(() => {
    if (skipInitialFetch.current) {
      skipInitialFetch.current = false;
      return;
    }

    const current = ++requestId.current;
    void listAdminProducts(query)
      .then((next) => {
        if (current === requestId.current) {
          setProducts(next);
          setLoadError(null);
        }
      })
      .catch(() => {
        if (current === requestId.current) {
          setLoadError("No se pudo cargar la lista de productos.");
        }
      });
  }, [query]);

  async function reload() {
    const current = ++requestId.current;
    const next = await listAdminProducts(query);
    if (current === requestId.current) {
      setProducts(next);
    }
  }

  function updateFilter<T>(setter: (value: T) => void, value: T) {
    setter(value);
    setPage(1);
  }

  function toggleSelection(product: AdminProduct) {
    const id = rowId(product);
    setSelectedIds((current) => {
      const next = new Set(current);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  function clearSelection() {
    setSelectedIds(new Set());
  }

  async function mutate(action: () => Promise<void>) {
    try {
      await action();
      clearSelection();
      await reload();
      setLoadError(null);
    } catch {
      setLoadError("No se pudo completar la acción.");
    }
  }

  async function handleExport() {
    setExporting(true);
    try {
      const { filename, content } = await exportAdminProducts(query);
      const url = URL.createObjectURL(
        new Blob([content], { type: "text/csv;charset=utf-8" }),
      );
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch {
      setLoadError("No se pudo exportar el CSV.");
    } finally {
      setExporting(false);
    }
  }

  function handleConfirm() {
    if (!confirmState) {
      return;
    }

    const { kind, ids } = confirmState;
    setConfirmState(null);
    void mutate(() =>
      kind === "archive"
        ? publishAdminProducts(ids, false)
        : deleteAdminProducts(ids),
    );
  }

  async function handleFormSave(payload: AdminProductPayload) {
    try {
      if (payload.productId) {
        await updateAdminProduct(payload);
      } else {
        await createAdminProduct(payload);
      }
      setFormState({ open: false });
      await reload();
      setLoadError(null);
    } catch {
      setLoadError("No se pudo guardar el producto.");
    }
  }

  const pageSize = products.pageSize || PAGE_SIZE;
  const pageCount = Math.max(1, Math.ceil(products.totalProducts / pageSize));
  const first = products.items.length ? (page - 1) * pageSize + 1 : 0;
  const last = products.items.length ? first + products.items.length - 1 : 0;

  return (
    <div className="flex flex-col gap-6">
      <AdminProductsHeader
        products={products}
        onExport={() => void handleExport()}
        onNewProduct={() => setFormState({ open: true })}
        exporting={exporting}
      />
      <AdminProductsToolbar
        categories={categories}
        search={search}
        status={status}
        categoryId={categoryId}
        sort={sort}
        onSearchChange={(value) => updateFilter(setSearch, value)}
        onStatusChange={(value) => updateFilter(setStatus, value)}
        onCategoryChange={(value) => updateFilter(setCategoryId, value)}
        onSortChange={(value) => updateFilter(setSort, value)}
      />
      {loadError ? (
        <p role="alert" className="text-sm text-wf-ink-secondary">
          {loadError}
        </p>
      ) : null}
      <AdminProductsBulkBar
        count={selectedIds.size}
        onPublish={() =>
          void mutate(() => publishAdminProducts(Array.from(selectedIds), true))
        }
        onArchive={() =>
          setConfirmState({ kind: "archive", ids: Array.from(selectedIds) })
        }
        onDelete={() =>
          setConfirmState({ kind: "delete", ids: Array.from(selectedIds) })
        }
        onClear={clearSelection}
      />
      <AdminProductsTable
        products={products}
        selectedIds={selectedIds}
        onToggleSelect={toggleSelection}
        onEdit={(product) => setFormState({ open: true, product })}
      />
      <AdminPagination
        label={`Mostrando ${first}–${last} de ${products.totalProducts} productos`}
        pageCount={pageCount}
        current={page}
        onPageChange={setPage}
      />

      {formState.open ? (
        <AdminProductFormModal
          product={formState.product}
          categories={categories}
          onClose={() => setFormState({ open: false })}
          onSave={(payload) => void handleFormSave(payload)}
        />
      ) : null}

      {confirmState ? (
        <AdminProductConfirmDialog
          kind={confirmState.kind}
          count={confirmState.ids.length}
          onCancel={() => setConfirmState(null)}
          onConfirm={handleConfirm}
        />
      ) : null}
    </div>
  );
}

type AdminProductsTableProps = {
  products: AdminProducts;
  selectedIds?: Set<string>;
  onToggleSelect?: (product: AdminProduct) => void;
  onEdit?: (product: AdminProduct) => void;
};

/** Products table: the frame's column widths and row height, now interactive. */
export function AdminProductsTable({
  products,
  selectedIds,
  onToggleSelect,
  onEdit,
}: AdminProductsTableProps) {
  const lowStockThreshold = products.lowStockThreshold;

  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="overflow-x-auto">
        <div role="table" aria-label="Productos" className="min-w-[1096px]">
          <div
            role="row"
            className="flex h-10 items-center border-b border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted"
          >
            <div role="columnheader" className="w-10" />
            <div role="columnheader" className="w-[440px]">
              Producto
            </div>
            <div role="columnheader" className="w-[150px]">
              Categoría
            </div>
            <div role="columnheader" className="w-[120px]">
              Precio
            </div>
            <div role="columnheader" className="w-[110px]">
              Stock
            </div>
            <div role="columnheader" className="w-[130px]">
              Estado
            </div>
            <div role="columnheader" className="flex-1 text-right">
              Acciones
            </div>
          </div>

          {products.items.map((product) => {
            const lowStock = product.stock < lowStockThreshold;
            const selected = selectedIds?.has(rowId(product)) ?? false;

            return (
              <div
                key={rowId(product)}
                role="row"
                className="flex h-16 items-center border-b border-wf-line px-5 text-sm last:border-b-0"
              >
                <div role="cell" className="w-10">
                  <button
                    type="button"
                    aria-label={`Seleccionar ${product.name}`}
                    aria-pressed={selected}
                    onClick={() => onToggleSelect?.(product)}
                    className={cn(
                      "flex size-6 items-center justify-center rounded-wf-sm border text-xs font-bold",
                      selected
                        ? "border-wf-inverse bg-wf-inverse text-wf-on-inverse"
                        : "border-wf-line-strong bg-wf-surface",
                    )}
                  >
                    {selected ? "✓" : ""}
                  </button>
                </div>

                <div
                  role="cell"
                  className="flex w-[440px] min-w-0 flex-col gap-1"
                >
                  <span className="truncate font-medium text-wf-ink">
                    {product.name}
                  </span>
                  <span className="text-xs text-wf-ink-muted">
                    {product.sku}
                  </span>
                </div>

                <div role="cell" className="w-[150px] text-wf-ink-secondary">
                  {product.category}
                </div>
                <div role="cell" className="w-[120px] font-medium text-wf-ink">
                  {formatPrice(product.price)}
                </div>
                <div role="cell" className="w-[110px]">
                  {lowStock ? (
                    <span className="inline-flex h-6 items-center rounded-full bg-wf-subtle px-2 text-xs font-bold text-wf-ink-secondary">
                      {product.stock} u.
                    </span>
                  ) : (
                    <span className="text-wf-ink-secondary">
                      {product.stock} u.
                    </span>
                  )}
                </div>
                <div role="cell" className="w-[130px]">
                  <ProductStatusBadge status={product.status} />
                </div>

                <div
                  role="cell"
                  className="flex flex-1 items-center justify-end gap-3"
                >
                  <button
                    type="button"
                    onClick={() => onEdit?.(product)}
                    className="font-medium text-wf-ink transition-colors hover:underline"
                  >
                    Editar
                  </button>
                  <span
                    aria-hidden
                    className="font-bold text-wf-ink-placeholder"
                  >
                    ···
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
