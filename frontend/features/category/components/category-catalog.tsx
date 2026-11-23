"use client";

import { useState } from "react";

import type { Category } from "@/features/product/types";
import { cn } from "@/lib/utils";

import { CategoryTile } from "./category-tile";

const ALL_FILTER = "todas";

const SORT_OPTIONS = [
  { value: "featured", label: "Destacados" },
  { value: "name", label: "Nombre A–Z" },
] as const;

type SortValue = (typeof SORT_OPTIONS)[number]["value"];

/**
 * Title row, toolbar and grid of the categories screen.
 *
 * It is a client component because the filter strip is stateful: selecting a
 * category narrows the grid in place, which is what the pills of the frame
 * look like they do; the sort control orders the same list in place.
 */
export function CategoryCatalog({ categories }: { categories: Category[] }) {
  const [activeFilter, setActiveFilter] = useState<string>(ALL_FILTER);
  const [sort, setSort] = useState<SortValue>("featured");

  const filters = categories.filter((category) => category.featured).slice(0, 4);
  const filtered =
    activeFilter === ALL_FILTER
      ? categories
      : categories.filter((category) => category.id === activeFilter);
  const visible = [...filtered].sort((a, b) =>
    sort === "name"
      ? a.name.localeCompare(b.name, "es")
      : Number(b.featured) - Number(a.featured) || a.name.localeCompare(b.name, "es"),
  );

  const totalLabel = categories.length === 1 ? "categoría" : "categorías";
  const visibleLabel = visible.length === 1 ? "categoría" : "categorías";

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-wf-ink">Categorías</h1>
          <p className="text-sm text-wf-ink-muted">
            Encontrá lo que buscás entre nuestras {categories.length} {totalLabel}.
          </p>
        </div>
        <span className="text-sm text-wf-ink-muted">
          {visible.length} {visibleLabel}
        </span>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div role="group" aria-label="Filtrar por categoría" className="flex flex-wrap items-center gap-2">
          <FilterPill
            label="Todas"
            active={activeFilter === ALL_FILTER}
            onSelect={() => setActiveFilter(ALL_FILTER)}
          />
          {filters.map((category) => (
            <FilterPill
              key={category.id}
              label={category.name}
              active={activeFilter === category.id}
              onSelect={() => setActiveFilter(category.id)}
            />
          ))}
        </div>

        <div className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm">
          <label htmlFor="category-sort" className="text-wf-ink-muted">
            Ordenar por:
          </label>
          <select
            id="category-sort"
            value={sort}
            onChange={(event) => setSort(event.target.value as SortValue)}
            className="bg-transparent font-medium text-wf-ink outline-none"
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {visible.map((category) => (
          <li key={category.id}>
            <CategoryTile category={category} />
          </li>
        ))}
      </ul>
    </div>
  );
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
