"use server";

import { revalidatePath } from "next/cache";

import { findCategoryBySlug } from "@/features/product/services/catalog.service";
import { httpClient } from "@/lib/http/client";

/**
 * Marks or unmarks a category in the Home grid.
 *
 * The action takes only the row id and the new flag; the rest of the update DTO
 * comes from the public category endpoint, which accepts both a numeric id
 * (real API) and a slug (mock seed). Both affected routes are revalidated so
 * the list and the Home wireframe show the change immediately.
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

  await httpClient.put("/api/categories", {
    categoryId: category.id,
    name: category.name,
    description: category.description ?? "",
    slug: category.slug,
    featured,
    productCount: category.productCount,
  });

  revalidatePath("/admin/categorias");
  revalidatePath("/");
}
