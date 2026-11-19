"use client";

import { useState } from "react";

import { ProductCard } from "@/features/product/components/product-card";
import type { Category, Product } from "@/features/product/types";
import { cn } from "@/lib/utils";

const ALL_FILTER = "todos";
const PAGE_SIZE = 12;

/** Order the mock catalog already returns; the frame has no menu for it. */
const SORT_LABEL = "Destacados";

type CategoryProductsProps = {
  category: Category;
  products: Product[];
};

/**
 * Title row, toolbar, product grid and pagination of the category screen.
 *
 * It is a client component because the filter strip and the page numbers are
 * state: selecting a subcategory narrows the grid and goes back to page one.
 * The sort control stays presentational because the design draws no menu for
 * it, same as on `/categorias`.
 */
export function CategoryProducts({ category, products }: CategoryProductsProps) {
  const [activeFilter, setActiveFilter] = useState<string>(ALL_FILTER);
  const [page, setPage] = useState(1);

  const filters = Array.from(new Set(products.map((product) => product.subcategory)));
  const filtered =
    activeFilter === ALL_FILTER
      ? products
      : products.filter((product) => product.subcategory === activeFilter);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount);
  const start = (currentPage - 1) * PAGE_SIZE;
  const visible = filtered.slice(start, start + PAGE_SIZE);

  function selectFilter(filter: string) {
    setActiveFilter(filter);
    setPage(1);
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-wf-ink">{category.name}</h1>
          {category.description ? (
            <p className="text-sm text-wf-ink-muted">{category.description}</p>
          ) : null}
        </div>
        <span className="text-sm text-wf-ink-muted">
          {filtered.length} {pluralize(filtered.length)}
        </span>
      </div>

      {products.length === 0 ? (
        <p className="text-sm text-wf-ink-muted">
          Todavía no hay productos en esta categoría.
        </p>
      ) : (
        <>
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div
              role="group"
              aria-label="Filtrar por subcategoría"
              className="flex flex-wrap items-center gap-2"
            >
              <FilterPill
                label="Todos"
                active={activeFilter === ALL_FILTER}
                onSelect={() => selectFilter(ALL_FILTER)}
              />
              {filters.map((filter) => (
                <FilterPill
                  key={filter}
                  label={filter}
                  active={activeFilter === filter}
                  onSelect={() => selectFilter(filter)}
                />
              ))}
            </div>

            <div className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
              <span className="text-wf-ink-muted">Ordenar por:</span>
              <span className="font-medium text-wf-ink">{SORT_LABEL}</span>
              <span aria-hidden className="text-wf-ink-placeholder">
                ↓
              </span>
            </div>
          </div>

          <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {visible.map((product) => (
              <li key={product.id} className="flex">
                <ProductCard product={product} variant="catalog" />
              </li>
            ))}
          </ul>

          <div className="flex flex-wrap items-center justify-between gap-4">
            <span className="text-sm text-wf-ink-muted">
              Mostrando {start + 1}–{start + visible.length} de {filtered.length}{" "}
              {pluralize(filtered.length)}
            </span>

            {pageCount > 1 ? (
              <nav aria-label="Paginación" className="flex items-center gap-2">
                <button
                  type="button"
                  aria-label="Página anterior"
                  disabled={currentPage === 1}
                  onClick={() => setPage(currentPage - 1)}
                  className={cn(
                    "flex size-10 items-center justify-center rounded-wf-sm border text-sm",
                    currentPage === 1
                      ? "border-wf-line bg-wf-surface text-wf-ink-placeholder"
                      : "border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
                  )}
                >
                  <span aria-hidden>←</span>
                </button>

                {Array.from({ length: pageCount }, (_, index) => index + 1).map(
                  (pageNumber) => (
                    <button
                      key={pageNumber}
                      type="button"
                      aria-label={`Página ${pageNumber}`}
                      aria-current={pageNumber === currentPage ? "page" : undefined}
                      onClick={() => setPage(pageNumber)}
                      className={cn(
                        "flex size-10 items-center justify-center rounded-wf-sm border text-sm",
                        pageNumber === currentPage
                          ? "border-wf-inverse bg-wf-inverse font-medium text-wf-on-inverse"
                          : "border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
                      )}
                    >
                      {pageNumber}
                    </button>
                  ),
                )}

                <button
                  type="button"
                  aria-label="Página siguiente"
                  disabled={currentPage === pageCount}
                  onClick={() => setPage(currentPage + 1)}
                  className={cn(
                    "flex size-10 items-center justify-center rounded-wf-sm border text-sm",
                    currentPage === pageCount
                      ? "border-wf-line bg-wf-surface text-wf-ink-placeholder"
                      : "border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
                  )}
                >
                  <span aria-hidden>→</span>
                </button>
              </nav>
            ) : null}
          </div>
        </>
      )}
    </div>
  );
}

function pluralize(count: number): string {
  return count === 1 ? "producto" : "productos";
}

type FilterPillProps = {
  label: string;
  active: boolean;
  onSelect: () => void;
};

/** 40px pill of the filter strip: inverse when selected, outlined otherwise. */
function FilterPill({ label, active, onSelect }: FilterPillProps) {
  return (
    <button
      type="button"
      aria-pressed={active}
      onClick={onSelect}
      className={cn(
        "flex h-10 items-center rounded-full px-4 text-sm transition-colors",
        active
          ? "bg-wf-inverse font-medium text-wf-on-inverse"
          : "border border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
      )}
    >
      {label}
    </button>
  );
}
