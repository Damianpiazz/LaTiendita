"use server";

import { isAxiosError } from "axios";
import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";

import { getAdminCategories } from "@/features/admin/services/admin.service";
import type {
  AdminCategories,
  AdminCategoriesQuery,
  AdminCategoryDeleteResult,
  AdminCategoryPayload,
} from "@/features/admin/types";
import { findCategoryBySlug } from "@/features/product/services/catalog.service";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

export type {
  AdminCategoryDeleteResult,
  AdminCategoryPayload,
} from "@/features/admin/types";

const CATEGORIES_PATH = "/admin/categorias";
const DELETE_GUARD_MESSAGE =
  "No se puede eliminar la categoría porque tiene productos asociados.";

type CategoryDetail = {
  categoryId?: string;
  name: string;
  slug: string;
  description?: string | null;
  featured?: boolean;
  imageUrl?: string | null;
};

/** Every category write requires the admin token; the gateway checks the role. */
async function adminHeaders(): Promise<Record<string, string>> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function toCategoryDto(payload: AdminCategoryPayload) {
  return {
    categoryId: payload.categoryId,
    name: payload.name.trim(),
    slug: payload.slug.trim(),
    description: payload.description?.trim() ?? "",
    featured: payload.featured,
    active: payload.active,
    imageUrl: payload.imageUrl?.trim() || null,
  };
}

/** Reuse the adapter so the list keeps its Bearer token and page conversion. */
export async function listAdminCategories(
  query: AdminCategoriesQuery = {},
): Promise<AdminCategories> {
  return getAdminCategories(query);
}

export async function createAdminCategory(
  payload: AdminCategoryPayload,
): Promise<void> {
  const headers = await adminHeaders();
  await httpClient.post("/api/categories", toCategoryDto(payload), { headers });
  revalidatePath(CATEGORIES_PATH);
}

export async function updateAdminCategory(
  payload: AdminCategoryPayload,
): Promise<void> {
  if (!payload.categoryId) {
    return;
  }

  const headers = await adminHeaders();
  await httpClient.put(
    `/api/categories/${encodeURIComponent(payload.categoryId)}`,
    toCategoryDto(payload),
    { headers },
  );
  revalidatePath(CATEGORIES_PATH);
}

/**
 * The 409 ProblemDetail is a normal outcome, not a transport failure: the
 * caller keeps the confirmation dialog open and shows the returned message.
 */
export async function deleteAdminCategory(
  categoryId: string,
): Promise<AdminCategoryDeleteResult> {
  if (!categoryId) {
    return { ok: false, error: "No se seleccionó ninguna categoría." };
  }

  const headers = await adminHeaders();
  try {
    await httpClient.delete(`/api/categories/${encodeURIComponent(categoryId)}`, {
      headers,
    });
    revalidatePath(CATEGORIES_PATH);
    return { ok: true };
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 409) {
      const data = error.response.data as { detail?: unknown } | undefined;
      const detail = typeof data?.detail === "string" ? data.detail.trim() : "";
      return { ok: false, error: detail || DELETE_GUARD_MESSAGE };
    }

    throw error;
  }
}

/**
 * The checkbox sends only the id and the new flag; the stored row is read back
 * so the slug and the Home-only `featured` flag survive the update.
 */
export async function setCategoryActive(
  categoryId: string,
  active: boolean,
): Promise<void> {
  if (!categoryId) {
    return;
  }

  const headers = await adminHeaders();
  const { data } = await httpClient.get<CategoryDetail>(
    `/api/categories/${encodeURIComponent(categoryId)}`,
    { headers },
  );

  await httpClient.put(
    `/api/categories/${encodeURIComponent(categoryId)}`,
    {
      categoryId: data.categoryId ?? categoryId,
      name: data.name,
      slug: data.slug,
      description: data.description ?? "",
      featured: Boolean(data.featured),
      active,
      imageUrl: data.imageUrl ?? null,
    },
    { headers },
  );
  revalidatePath(CATEGORIES_PATH);
}

/**
 * Marks or unmarks a category in the Home grid.
 *
 * The action takes only the row id and the new flag; the rest of the update DTO
 * comes from the public category endpoint. Both affected routes are revalidated
 * so the list and the Home wireframe show the change immediately.
 */
export async function setCategoryFeatured(formData: FormData): Promise<void> {
  const categoryId = String(formData.get("categoryId") ?? "");
  const featured = String(formData.get("featured")) === "true";

  if (!categoryId) {
    return;
  }

  const category = await findCategoryBySlug(categoryId);

  if (!category) {
    return;
  }

  const headers = await adminHeaders();
  await httpClient.put(
    "/api/categories",
    {
      categoryId: category.id,
      name: category.name,
      description: category.description ?? "",
      slug: category.slug,
      featured,
      productCount: category.productCount,
    },
    { headers },
  );

  revalidatePath(CATEGORIES_PATH);
  revalidatePath("/");
}
