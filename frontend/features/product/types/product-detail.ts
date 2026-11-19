import type { Product } from "./product";

export type ProductSpec = {
  label: string;
  value: string;
};

/**
 * Product as the detail screen needs it.
 *
 * It extends the catalog card shape with the fields the wireframe renders;
 * `product-service` documents description, specs and ratings as pending, so the
 * mock is the only source for them until the API grows those fields.
 */
export type ProductDetail = Product & {
  sku: string;
  /** Price before the discount; `null` when the product has no discount. */
  originalPrice: number | null;
  /** Interest-free installments offered for the price. */
  installmentCount: number;
  stock: number;
  /** Promised dispatch window for in-stock units, e.g. `24/48 hs`. */
  fulfillment: string;
  description: string[];
  highlights: string[];
  specs: ProductSpec[];
  rating: number;
  reviewCount: number;
  soldCount: number;
  galleryCount: number;
};
