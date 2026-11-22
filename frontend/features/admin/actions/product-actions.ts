"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";

import { getAdminProducts } from "@/features/admin/services/admin.service";
import type {
  AdminProductPayload,
  AdminProducts,
  AdminProductsQuery,
} from "@/features/admin/types";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

export type { AdminProductPayload } from "@/features/admin/types";

const PRODUCTS_PATH = "/admin/productos";
const MAX_PRODUCT_IMAGES = 10;

function galleryImages(values: unknown): string[] {
  if (!Array.isArray(values)) {
    return [];
  }

  return values
    .filter((value): value is string => typeof value === "string")
    .map((value) => value.trim())
    .filter((value) => value.length > 0)
    .slice(0, MAX_PRODUCT_IMAGES);
}

/**
 * The admin gateway requires an admin token on every product write and on the
 * whole `/api/products/admin/**` tree. Unlike the category action, these calls
 * must propagate the cookie explicitly.
 */
async function adminHeaders(): Promise<Record<string, string>> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** Reuse the adapter instead of duplicating the real/mock branching. */
export async function listAdminProducts(
  query: AdminProductsQuery,
): Promise<AdminProducts> {
  return getAdminProducts(query);
}

/**
 * The admin list DTO has no gallery; edit reads the real detail before opening
 * the form so the ten image slots can be restored and sent back on save.
 */
export async function getAdminProductImages(
  productId: string,
): Promise<string[]> {
  const headers = await adminHeaders();
  const { data } = await httpClient.get<{
    images?: string[] | null;
    imageUrl?: string | null;
  }>(`/api/products/${encodeURIComponent(productId)}`, { headers });
  const detail = data ?? {};
  const gallery = galleryImages(detail.images);

  return gallery.length > 0 ? gallery : galleryImages([detail.imageUrl ?? ""]);
}

function toCatalogDto(payload: AdminProductPayload) {
  return {
    ...(payload.productId ? { productId: Number(payload.productId) } : {}),
    title: payload.title,
    sku: payload.sku,
    ...(payload.categoryId ? { categoryId: Number(payload.categoryId) } : {}),
    price: payload.price,
    quantity: payload.quantity,
    description: payload.description ? [payload.description] : [],
    images: galleryImages(payload.images),
    published: payload.published,
  };
}

export async function createAdminProduct(
  payload: AdminProductPayload,
): Promise<void> {
  const headers = await adminHeaders();
  await httpClient.post("/api/products", toCatalogDto(payload), { headers });
  revalidatePath(PRODUCTS_PATH);
}

export async function updateAdminProduct(
  payload: AdminProductPayload,
): Promise<void> {
  if (!payload.productId) {
    return;
  }

  const headers = await adminHeaders();
  await httpClient.put(
    `/api/products/${encodeURIComponent(payload.productId)}`,
    toCatalogDto(payload),
    { headers },
  );
  revalidatePath(PRODUCTS_PATH);
}

export async function publishAdminProducts(
  productIds: string[],
  published: boolean,
): Promise<void> {
  if (productIds.length === 0) {
    return;
  }

  const headers = await adminHeaders();
  await httpClient.post(
    "/api/products/admin/status",
    { productIds: productIds.map(Number), published },
    { headers },
  );
  revalidatePath(PRODUCTS_PATH);
}

export async function deleteAdminProducts(
  productIds: string[],
): Promise<void> {
  if (productIds.length === 0) {
    return;
  }

  const headers = await adminHeaders();
  await httpClient.delete("/api/products/admin", {
    data: { productIds: productIds.map(Number) },
    headers,
  });
  revalidatePath(PRODUCTS_PATH);
}

export async function exportAdminProducts(
  query: AdminProductsQuery,
): Promise<{ filename: string; content: string }> {
  const headers = await adminHeaders();
  const params: Record<string, string> = {};
  if (query.q) params.q = query.q;
  if (query.status && query.status !== "all") params.status = query.status;
  if (query.categoryId) params.categoryId = query.categoryId;
  if (query.sort) params.sort = query.sort;

  const { data } = await httpClient.get<string>("/api/products/admin/export.csv", {
    params,
    headers: { ...headers, Accept: "text/csv" },
    responseType: "text",
  });

  return { filename: "productos.csv", content: String(data) };
}
