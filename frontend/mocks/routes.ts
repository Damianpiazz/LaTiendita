import type { MockRoute } from "./adapter";
import {
  adminCategories,
  adminClients,
  adminNavigation,
  adminOrders,
  adminPayments,
  adminProducts,
  adminSummary,
} from "./data/admin";
import { categories } from "./data/categories";
import { customerProfile } from "./data/customer";
import { productDetails } from "./data/product-details";
import { products } from "./data/products";
import { getReviewSummaryMock, getReviewsMock } from "./data/reviews";
import { shippingMethods } from "./data/shipping";

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

/**
 * Mock of the catalog, review and admin APIs.
 *
 * Paths and query params match the contract the backend will expose, so the
 * services do not change when `NEXT_PUBLIC_USE_MOCKS` is turned off.
 */
export const mockRoutes: MockRoute[] = [
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
    path: "/admin/payments",
    handler: () => adminPayments,
  },
  {
    method: "GET",
    path: "/admin/clients",
    handler: () => adminClients,
  },
  {
    method: "GET",
    path: "/customer/profile",
    handler: () => customerProfile,
  },
  {
    method: "GET",
    path: "/categories",
    handler: ({ searchParams }) => {
      const featured = readFeatured(searchParams);

      return featured === undefined
        ? categories
        : categories.filter((category) => category.featured === featured);
    },
  },
  {
    method: "GET",
    path: "/categories/:slug",
    handler: ({ params }) =>
      categories.find((category) => category.slug === params.slug),
  },
  {
    method: "GET",
    path: "/products",
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
    path: "/products/:slug",
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
