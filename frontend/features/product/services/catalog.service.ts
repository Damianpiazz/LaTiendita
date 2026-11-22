import { cacheLife } from "next/cache";
import { isAxiosError } from "axios";

import type {
  Category,
  Product,
  ProductDetail,
  ProductSpec,
} from "@/features/product/types";
import { httpClient } from "@/lib/http/client";

type ProductFilters = {
  featured?: boolean;
  categoryId?: string;
  size?: number;
};

type Page<T> = {
  content: T[];
};

/** backend CategoryDto plus the frontend-shaped seed served by the mock adapter */
type CategoryDto = {
  categoryId?: string | number;
  id?: string | number;
  name: string;
  description?: string | null;
  slug: string;
  featured?: boolean;
  productCount?: number;
};

/** backend ProductDto aliases and record fields, plus the frontend-shaped seed */
type ProductDto = {
  productId?: string | number;
  id?: string | number;
  title?: string;
  name?: string;
  price: string | number;
  categoryId: string | number;
  categoryName?: string | null;
  subcategory?: string | null;
  slug: string;
  featured?: boolean;
  sku?: string;
  originalPrice?: string | number | null;
  quantity?: number;
  stock?: number;
  fulfillment?: string | null;
  images?: string[];
  description?: string[];
  highlights?: string[];
  specs?: ProductSpec[];
  galleryCount?: number;
  installmentCount?: number;
  rating?: number;
  reviewCount?: number;
  soldCount?: number;
};

/** The real API serves Spring `Page`; mock mode still serves plain arrays. */
function unwrapPage<T>(payload: Page<T> | T[]): T[] {
  return Array.isArray(payload) ? payload : payload.content;
}

function toCategory(dto: CategoryDto): Category {
  return {
    id: String(dto.categoryId ?? dto.id),
    name: dto.name,
    slug: dto.slug,
    productCount: Number(dto.productCount ?? 0),
    ...(dto.description ? { description: dto.description } : {}),
    featured: Boolean(dto.featured),
  };
}

function toProduct(dto: ProductDto): Product {
  return {
    id: String(dto.id ?? dto.productId),
    name: dto.name ?? dto.title ?? "",
    price: Number(dto.price),
    categoryId: String(dto.categoryId),
    categoryName: dto.categoryName ?? "",
    subcategory: dto.subcategory ?? "",
    slug: dto.slug,
    featured: Boolean(dto.featured),
  };
}

function toProductDetail(dto: ProductDto): ProductDetail {
  return {
    ...toProduct(dto),
    sku: dto.sku ?? "",
    originalPrice: dto.originalPrice === null || dto.originalPrice === undefined
      ? null
      : Number(dto.originalPrice),
    installmentCount: dto.installmentCount ?? 1,
    stock: Number(dto.stock ?? dto.quantity ?? 0),
    fulfillment: dto.fulfillment ?? "",
    description: dto.description ?? [],
    highlights: dto.highlights ?? [],
    specs: dto.specs ?? [],
    rating: dto.rating ?? 0,
    reviewCount: dto.reviewCount ?? 0,
    soldCount: dto.soldCount ?? 0,
    galleryCount: dto.galleryCount ?? dto.images?.length ?? 0,
  };
}

export async function getCategories({ featured }: { featured?: boolean } = {}): Promise<Category[]> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<Page<CategoryDto> | CategoryDto[]>("/api/categories", {
    params: {
      size: 100,
      ...(featured === undefined ? {} : { featured }),
    },
  });

  return unwrapPage(data).map(toCategory);
}

export async function getCategoryBySlug(slug: string): Promise<Category> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<CategoryDto>(
    `/api/categories/${encodeURIComponent(slug)}`,
  );

  return toCategory(data);
}

export async function getProducts({
  featured,
  categoryId,
  size,
}: ProductFilters = {}): Promise<Product[]> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<Page<ProductDto> | ProductDto[]>("/api/products", {
    params: {
      ...(size === undefined ? {} : { size }),
      ...(featured === undefined ? {} : { featured }),
      ...(categoryId === undefined ? {} : { categoryId }),
    },
  });

  return unwrapPage(data).map(toProduct);
}

export function getFeaturedProducts(): Promise<Product[]> {
  return getProducts({ featured: true });
}

/** Listing of one category's products; the screen filters and paginates it. */
export function getCategoryProducts(categoryId: string): Promise<Product[]> {
  return getProducts({ categoryId, size: 100 });
}

export async function getProductBySlug(slug: string): Promise<ProductDetail> {
  "use cache";
  cacheLife("hours");

  const { data } = await httpClient.get<ProductDto>(
    `/api/products/${encodeURIComponent(slug)}`,
  );

  return toProductDetail(data);
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
