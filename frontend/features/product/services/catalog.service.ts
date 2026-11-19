import { cacheLife } from "next/cache";
import { isAxiosError } from "axios";

import type { Category, Product, ProductDetail } from "@/features/product/types";
import { httpClient } from "@/lib/http/client";

type ProductFilters = {
  featured?: boolean;
  categoryId?: string;
};

export async function getCategories({ featured }: { featured?: boolean } = {}): Promise<Category[]> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<Category[]>("/categories", {
    params: featured === undefined ? {} : { featured },
  });

  return data;
}

export async function getCategoryBySlug(slug: string): Promise<Category> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<Category>(
    `/categories/${encodeURIComponent(slug)}`,
  );

  return data;
}

export async function getProducts({
  featured,
  categoryId,
}: ProductFilters = {}): Promise<Product[]> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<Product[]>("/products", {
    params: {
      ...(featured === undefined ? {} : { featured }),
      ...(categoryId === undefined ? {} : { categoryId }),
    },
  });

  return data;
}

export function getFeaturedProducts(): Promise<Product[]> {
  return getProducts({ featured: true });
}

/** Listing of one category's products; the screen filters and paginates it. */
export function getCategoryProducts(categoryId: string): Promise<Product[]> {
  return getProducts({ categoryId });
}

export async function getProductBySlug(slug: string): Promise<ProductDetail> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<ProductDetail>(
    `/products/${encodeURIComponent(slug)}`,
  );

  return data;
}

/**
 * Related grid of the product screen.
 *
 * The wireframe shows the first four featured products, so the helper slices
 * the cached featured list; only this function changes when the catalog API
 * exposes a recommendations endpoint.
 */
export async function getRelatedProducts(limit = 4): Promise<Product[]> {
  const featured = await getFeaturedProducts();

  return featured.slice(0, limit);
}

/** Detail lookup that turns a 404 into `null` so the route can render `notFound()`. */
export async function findProductBySlug(slug: string): Promise<ProductDetail | null> {
  try {
    return await getProductBySlug(slug);
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return null;
    }

    throw error;
  }
}

/** Category lookup that turns a 404 into `null` so the route can render `notFound()`. */
export async function findCategoryBySlug(slug: string): Promise<Category | null> {
  try {
    return await getCategoryBySlug(slug);
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return null;
    }

    throw error;
  }
}
