"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import {
  createAdminCategory,
  deleteAdminCategory,
  listAdminCategories,
  setCategoryActive,
  updateAdminCategory,
} from "@/features/admin/actions/category-actions";
import { AdminCategoriesHeader } from "@/features/admin/components/admin-categories-header";
import { AdminCategoriesToolbar } from "@/features/admin/components/admin-categories-toolbar";
import { AdminCategoryConfirmDialog } from "@/features/admin/components/admin-category-confirm-dialog";
import { AdminCategoryFormModal } from "@/features/admin/components/admin-category-form-modal";
import { CategoryDetailCard } from "@/features/admin/components/category-detail-card";
import { CategoryList } from "@/features/admin/components/category-list";
import type {
  AdminCategories,
  AdminCategoriesQuery,
  AdminCategory,
  AdminCategoryPayload,
} from "@/features/admin/types";

const PAGE_SIZE = 6;
const DEFAULT_SORT = "name,asc";
const REVERSE_SORT = "name,desc";

type FormState = { open: false } | { open: true; category?: AdminCategory };
type DeleteState =
  | { open: false }
  | { open: true; category: AdminCategory; error: string | null };

/**
 * Interactive categories screen. The server seeds the first page; filters,
 * selection and every mutation go through the same category server actions.
 */
export function AdminCategoriesWorkspace({
  initialCategories,
}: {
  initialCategories: AdminCategories;
}) {
  const [categories, setCategories] = useState(initialCategories);
  const [selectedId, setSelectedId] = useState(initialCategories.selectedId);
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [formState, setFormState] = useState<FormState>({ open: false });
  const [deleteState, setDeleteState] = useState<DeleteState>({ open: false });
  const [loadError, setLoadError] = useState<string | null>(null);
  const requestId = useRef(0);

  const trimmedSearch = search.trim();
  const query = useMemo<AdminCategoriesQuery>(
    () => ({
      ...(trimmedSearch ? { q: trimmedSearch } : {}),
      page,
      size: PAGE_SIZE,
      sort,
    }),
    [trimmedSearch, page, sort],
  );

  const applyCategories = useCallback((next: AdminCategories) => {
    setCategories(next);
    setSelectedId((current) =>
      next.items.some((item) => item.id === current)
        ? current
        : (next.items.find((item) => item.id === next.selectedId)?.id ??
          next.items[0]?.id ??
          ""),
    );
  }, []);

  useEffect(() => {
    const current = ++requestId.current;
    void listAdminCategories(query)
      .then((next) => {
        if (current === requestId.current) {
          applyCategories(next);
          setLoadError(null);
        }
      })
      .catch(() => {
        if (current === requestId.current) {
          setLoadError("No se pudieron cargar las categorías.");
        }
      });
  }, [query, applyCategories]);

  async function reload() {
    const current = ++requestId.current;
    const next = await listAdminCategories(query);
    if (current === requestId.current) {
      applyCategories(next);
    }
  }

  function updateSearch(value: string) {
    setSearch(value);
    setPage(1);
  }

  function toggleSort() {
    setSort((current) => (current === DEFAULT_SORT ? REVERSE_SORT : DEFAULT_SORT));
    setPage(1);
  }

  async function handleFormSave(payload: AdminCategoryPayload) {
    try {
      if (payload.categoryId) {
        await updateAdminCategory(payload);
      } else {
        await createAdminCategory(payload);
      }
      setFormState({ open: false });
      await reload();
      setLoadError(null);
    } catch {
      setLoadError("No se pudo guardar la categoría.");
    }
  }

  async function handleActiveChange(category: AdminCategory, active: boolean) {
    try {
      await setCategoryActive(category.id, active);
      await reload();
      setLoadError(null);
    } catch {
      setLoadError("No se pudo actualizar la categoría.");
    }
  }

  async function handleDeleteConfirm() {
    if (!deleteState.open) {
      return;
    }

    const { category } = deleteState;
    try {
      const result = await deleteAdminCategory(category.id);
      if (!result.ok) {
        setDeleteState({ open: true, category, error: result.error });
        return;
      }

      setDeleteState({ open: false });
      await reload();
      setLoadError(null);
    } catch {
      setDeleteState({
        open: true,
        category,
        error: "No se pudo eliminar la categoría.",
      });
    }
  }

  const selected =
    categories.items.find((item) => item.id === selectedId) ??
    categories.items.find((item) => item.id === categories.selectedId) ??
    categories.items[0];

  return (
    <div className="flex flex-col gap-6">
      <AdminCategoriesHeader
        categories={categories}
        onCreate={() => setFormState({ open: true })}
      />
      <AdminCategoriesToolbar
        search={search}
        sort={sort}
        onSearchChange={updateSearch}
        onSortChange={toggleSort}
      />

      {loadError ? (
        <p role="alert" className="text-sm text-wf-ink-secondary">
          {loadError}
        </p>
      ) : null}

      <div className="flex flex-col gap-6 xl:flex-row xl:items-start">
        <div className="min-w-0 flex-1">
          <CategoryList
            items={categories.items}
            selectedId={selected?.id ?? ""}
            updatedLabel={categories.updatedLabel}
            onSelect={(category) => setSelectedId(category.id)}
          />
        </div>

        {selected ? (
          <CategoryDetailCard
            category={selected}
            onEdit={() => setFormState({ open: true, category: selected })}
            onDelete={() =>
              setDeleteState({ open: true, category: selected, error: null })
            }
            onActiveChange={(active) => void handleActiveChange(selected, active)}
          />
        ) : null}
      </div>

      {formState.open ? (
        <AdminCategoryFormModal
          category={formState.category}
          onClose={() => setFormState({ open: false })}
          onSave={(payload) => void handleFormSave(payload)}
        />
      ) : null}

      {deleteState.open ? (
        <AdminCategoryConfirmDialog
          category={deleteState.category}
          error={deleteState.error}
          onCancel={() => setDeleteState({ open: false })}
          onConfirm={() => void handleDeleteConfirm()}
        />
      ) : null}
    </div>
  );
}
