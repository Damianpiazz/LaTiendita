"use client";

import { useState } from "react";

import type { Category } from "@/features/product/types";
import { cn } from "@/lib/utils";

import { CategoryTile } from "./category-tile";

const ALL_FILTER = "todas";

/** Order the mock catalog already returns; the frame has no menu for it. */
const SORT_LABEL = "Destacados";

/**
 * Title row, toolbar and grid of the categories screen.
 *
 * It is a client component because the filter strip is stateful: selecting a
 * category narrows the grid in place, which is what the pills of the frame
 * look like they do. The sort control stays presentational because the design
 * draws no menu for it.
 */
export function CategoryCatalog({ categories }: { categories: Category[] }) {
  const [activeFilter, setActiveFilter] = useState<string>(ALL_FILTER);

  const filters = categories.filter((category) => category.featured).slice(0, 4);
  const visible =
    activeFilter === ALL_FILTER
      ? categories
      : categories.filter((category) => category.id === activeFilter);

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
          <span className="text-wf-ink-muted">Ordenar por:</span>
          <span className="font-medium text-wf-ink">{SORT_LABEL}</span>
          <span aria-hidden className="text-wf-ink-placeholder">
            ↓
          </span>
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
