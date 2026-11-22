import type {
  AdminProduct,
  AdminProductStatus,
} from "@/features/admin/types";

/** Frozen `AdminProductResponse` served by `GET /api/products/admin` (spec S3). */
export type AdminProductApi = {
  productId: string | number;
  name: string;
  sku: string;
  category: string;
  price: number | string;
  stock: number;
  status: string;
  published: boolean;
};

/** Frozen `AdminProductSummaryResponse` served by the summary endpoint (spec S3). */
export type AdminProductSummaryApi = {
  totalProducts: number | string;
  totalCategories: number | string;
  totalLowStock: number | string;
  lowStockThreshold: number | string;
};

const PRODUCT_STATUSES: AdminProductStatus[] = [
  "publicado",
  "borrador",
  "agotado",
];

function asNumber(value: number | string): number {
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

/**
 * Maps one backend row to the shape the wireframe table already renders.
 *
 * `id` keeps being the stable row key (the SKU in mock mode, the numeric id
 * string in real mode); `productId` is added so the edit action can address the
 * real endpoint without guessing from the SKU.
 */
export function mapAdminProduct(dto: AdminProductApi): AdminProduct {
  const status = PRODUCT_STATUSES.includes(dto.status as AdminProductStatus)
    ? (dto.status as AdminProductStatus)
    : dto.published
      ? "publicado"
      : "borrador";

  return {
    id: String(dto.productId),
    productId: String(dto.productId),
    name: dto.name,
    sku: dto.sku,
    category: dto.category,
    price: asNumber(dto.price),
    stock: asNumber(dto.stock),
    status,
    published: Boolean(dto.published),
  };
}

/** Real store totals replace the numbers previously copied from the seed. */
export function mapAdminProductSummary(api: AdminProductSummaryApi) {
  return {
    totalProducts: asNumber(api.totalProducts),
    totalCategories: asNumber(api.totalCategories),
    totalLowStock: asNumber(api.totalLowStock),
    lowStockThreshold: asNumber(api.lowStockThreshold),
  };
}
