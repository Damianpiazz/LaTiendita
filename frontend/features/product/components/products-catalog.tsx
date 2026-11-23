"use client";

import { useState } from "react";

import { ProductCard } from "@/features/product/components/product-card";
import type { Category, Product } from "@/features/product/types";
import { cn } from "@/lib/utils";

const PAGE_SIZE = 12;

const SORT_OPTIONS = [
  { value: "featured", label: "Destacados" },
  { value: "name", label: "Nombre A–Z" },
  { value: "price-asc", label: "Menor precio" },
  { value: "price-desc", label: "Mayor precio" },
] as const;

type SortValue = (typeof SORT_OPTIONS)[number]["value"];

type ProductsCatalogProps = {
  products: Product[];
  categories: Category[];
  initialQuery?: string;
};

function sortProducts(products: Product[], sort: SortValue): Product[] {
  const result = [...products];

  switch (sort) {
    case "name":
      return result.sort((a, b) => a.name.localeCompare(b.name, "es"));
    case "price-asc":
      return result.sort((a, b) => a.price - b.price);
    case "price-desc":
      return result.sort((a, b) => b.price - a.price);
    default:
      return result.sort(
        (a, b) => Number(b.featured) - Number(a.featured) || a.name.localeCompare(b.name, "es"),
      );
  }
}

/** Full catalog: search, category filter, sorting and client-side pagination. */
export function ProductsCatalog({ products, categories, initialQuery = "" }: ProductsCatalogProps) {
  const [query, setQuery] = useState(initialQuery);
  const [categoryId, setCategoryId] = useState("all");
  const [sort, setSort] = useState<SortValue>("featured");
  const [page, setPage] = useState(1);

  const term = query.trim().toLocaleLowerCase("es");
  const filtered = sortProducts(
    products.filter((product) => {
      const matchesCategory = categoryId === "all" || product.categoryId === categoryId;
      const matchesQuery =
        !term ||
        product.name.toLocaleLowerCase("es").includes(term) ||
        product.categoryName.toLocaleLowerCase("es").includes(term) ||
        product.subcategory.toLocaleLowerCase("es").includes(term);

      return matchesCategory && matchesQuery;
    }),
    sort,
  );

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount);
  const start = (currentPage - 1) * PAGE_SIZE;
  const visible = filtered.slice(start, start + PAGE_SIZE);

  function selectCategory(value: string) {
    setCategoryId(value);
    setPage(1);
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Productos</h1>
        <p className="text-sm text-wf-ink-muted">
          {filtered.length} {filtered.length === 1 ? "producto" : "productos"} disponibles
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <div
          role="search"
          className="flex h-10 w-full items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 sm:w-[300px]"
        >
          <label htmlFor="catalog-search" className="sr-only">
            Buscar en el catálogo
          </label>
          <input
            id="catalog-search"
            type="search"
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
              setPage(1);
            }}
            placeholder="Buscar por nombre o categoría..."
            className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
        </div>

        <label htmlFor="catalog-category" className="text-sm text-wf-ink-muted">
          Categoría
        </label>
        <select
          id="catalog-category"
          value={categoryId}
          onChange={(event) => selectCategory(event.target.value)}
          className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink-secondary outline-none"
        >
          <option value="all">Todas</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>

        <label htmlFor="catalog-sort" className="text-sm text-wf-ink-muted">
          Ordenar por
        </label>
        <select
          id="catalog-sort"
          value={sort}
          onChange={(event) => {
            setSort(event.target.value as SortValue);
            setPage(1);
          }}
          className="h-10 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink-secondary outline-none"
        >
          {SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {visible.length === 0 ? (
        <p className="rounded-wf-md border border-wf-line bg-wf-surface p-10 text-center text-sm text-wf-ink-muted">
          No encontramos productos con esos filtros.
        </p>
      ) : (
        <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {visible.map((product) => (
            <li key={product.id} className="flex">
              <ProductCard product={product} variant="catalog" />
            </li>
          ))}
        </ul>
      )}

      <div className="flex flex-wrap items-center justify-between gap-4">
        <span className="text-sm text-wf-ink-muted">
          Mostrando {visible.length ? start + 1 : 0}–{start + visible.length} de {filtered.length}
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
            {Array.from({ length: pageCount }, (_, index) => index + 1).map((pageNumber) => (
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
            ))}
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
    </div>
  );
}
