import type { MockRoute } from "./adapter";
import {
  adminCategories,
  adminNavigation,
  adminOrders,
  adminProducts,
  adminSummary,
} from "./data/admin";
import { categories } from "./data/categories";
import { customerProfile } from "./data/customer";
import { productDetails } from "./data/product-details";
import { products } from "./data/products";
import { getReviewSummaryMock, getReviewsMock } from "./data/reviews";
import { shippingMethods } from "./data/shipping";
import { mockAuth } from "./data/auth";

function readFeatured(searchParams: URLSearchParams): boolean | undefined {
  const value = searchParams.get("featured");

  if (value === null) {
    return undefined;
  }

  return ["1", "true", "yes", "on"].includes(value.trim().toLowerCase());
}

function readPositiveInt(searchParams: URLSearchParams, key: string, fallback: number): number {
  const value = Number(searchParams.get(key));

  return Number.isInteger(value) && value > 0 ? value : fallback;
}

function readBody<T>(data: unknown): T {
  return typeof data === "string" ? (JSON.parse(data) as T) : (data as T);
}

type CategoryUpdate = {
  categoryId?: string;
  id?: string;
  featured?: boolean;
};

/**
 * Mock of the catalog, review and admin APIs.
 *
 * Paths and query params match the contract the backend will expose, so the
 * services do not change when `NEXT_PUBLIC_USE_MOCKS` is turned off.
 */
export const mockRoutes: MockRoute[] = [
  {
    method: "POST",
    path: "/api/auth/login",
    handler: () => ({ user: mockAuth.login() }),
  },
  {
    method: "POST",
    path: "/api/auth/signup",
    handler: () => ({ user: mockAuth.login() }),
  },
  {
    method: "POST",
    path: "/api/auth/forgot-password",
    handler: () => ({
      message:
        "If an account exists for this email, a password reset link has been sent. Please check your inbox.",
    }),
  },
  {
    method: "POST",
    path: "/api/auth/skip-mfa",
    handler: () => ({ user: mockAuth.login() }),
  },
  {
    method: "POST",
    path: "/api/auth/logout",
    handler: () => mockAuth.logout(),
  },
  {
    method: "GET",
    path: "/api/auth/session",
    handler: () => mockAuth.session(),
  },
  {
    method: "GET",
    path: "/admin/summary",
    handler: () => adminSummary,
  },
  {
    method: "GET",
    path: "/admin/navigation",
    handler: () => adminNavigation,
  },
  {
    method: "GET",
    path: "/admin/categories",
    handler: () => adminCategories,
  },
  {
    method: "GET",
    path: "/admin/products",
    handler: () => adminProducts,
  },
  {
    method: "GET",
    path: "/admin/orders",
    handler: () => adminOrders,
  },
  {
    method: "GET",
    path: "/customer/profile",
    handler: () => customerProfile,
  },
  {
    method: "GET",
    path: "/api/categories",
    handler: ({ searchParams }) => {
      const featured = readFeatured(searchParams);

      return featured === undefined
        ? categories
        : categories.filter((category) => category.featured === featured);
    },
  },
  {
    method: "PUT",
    path: "/api/categories",
    handler: ({ data }) => {
      const update = readBody<CategoryUpdate>(data);
      const id = String(update.categoryId ?? update.id ?? "");
      const featured = Boolean(update.featured);
      const catalog = categories.find((category) => category.id === id || category.slug === id);
      const admin = adminCategories.items.find((category) => category.id === id || category.slug === id);

      if (catalog) {
        catalog.featured = featured;
      }

      if (admin) {
        admin.featured = featured;
      }

      return { ...update, featured };
    },
  },
  {
    method: "GET",
    path: "/api/categories/:slug",
    handler: ({ params }) =>
      categories.find((category) => category.slug === params.slug),
  },
  {
    method: "GET",
    path: "/api/products",
    handler: ({ searchParams }) => {
      const featured = readFeatured(searchParams);
      const categoryId = searchParams.get("categoryId");
      const subcategory = searchParams.get("subcategory");

      return products.filter(
        (product) =>
          (featured === undefined || product.featured === featured) &&
          (categoryId === null || product.categoryId === categoryId) &&
          (subcategory === null || product.subcategory === subcategory),
      );
    },
  },
  {
    method: "GET",
    path: "/api/products/:slug",
    handler: ({ params }) =>
      productDetails.find((product) => product.slug === params.slug),
  },
  {
    method: "GET",
    path: "/shipping/methods",
    handler: () => shippingMethods,
  },
  {
    method: "GET",
    path: "/reviews/summary",
    handler: ({ searchParams }) => {
      const productId = searchParams.get("productId") ?? "";

      return getReviewSummaryMock(productId);
    },
  },
  {
    method: "GET",
    path: "/reviews",
    handler: ({ searchParams }) => {
      const productId = searchParams.get("productId") ?? "";
      const page = readPositiveInt(searchParams, "page", 1);
      const size = readPositiveInt(searchParams, "size", 10);
      const summary = getReviewSummaryMock(productId);

      return {
        items: getReviewsMock(productId, page, size),
        page,
        size,
        total: summary.count,
      };
    },
  },
];
