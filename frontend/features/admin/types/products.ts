/** Product states the admin table renders as pills. */
export type AdminProductStatus = "publicado" | "borrador" | "agotado";

/** One row of the admin products table. */
export type AdminProduct = {
  /** Stable row key: the SKU in mock mode, the id string in real mode. */
  id: string;
  /** Real backend id, sent by the edit action. Absent on seeded rows. */
  productId?: string;
  name: string;
  sku: string;
  category: string;
  price: number;
  stock: number;
  status: AdminProductStatus;
  /** Real publication flag; absent on seeded rows. */
  published?: boolean;
  description?: string;
  /** Gallery references; the first entry is the main image. */
  images?: string[];
  /** Marks the row checked and feeds the bulk bar count. */
  selected?: boolean;
};

/** Status pill selection; `all` sends no `status` query param. */
export type AdminProductStatusFilter = "all" | AdminProductStatus;

/** Label/value pair persisted in `ProductDto.specs`. */
export type AdminProductSpec = {
  label: string;
  value: string;
};

/** Payload of the product form; the backend DTO is built at the action boundary. */
export type AdminProductPayload = {
  productId?: string;
  title: string;
  sku: string;
  categoryId?: string;
  price: number;
  quantity: number;
  description?: string;
  /** Stable catalog slug; the backend derives one when omitted. */
  slug?: string;
  subcategory?: string;
  featured?: boolean;
  /** Price before discount; `null` when the product has no discount. */
  originalPrice?: number | null;
  fulfillment?: string;
  highlights?: string[];
  specs?: AdminProductSpec[];
  /** Up to ten gallery references; the first one is the main image. */
  images: string[];
  published: boolean;
};

/** Query accepted by the admin products adapter and its server actions. */
export type AdminProductsQuery = {
  q?: string;
  status?: AdminProductStatusFilter;
  categoryId?: string;
  page?: number;
  size?: number;
  sort?: string;
};

/** `GET /admin/products` seed. */
export type AdminProducts = {
  items: AdminProduct[];
  /** Table size, e.g. 8, and store totals, e.g. 48. */
  pageSize: number;
  totalProducts: number;
  totalCategories: number;
  /** Units under this value draw as the low-stock pill. */
  lowStockThreshold: number;
  totalLowStock: number;
};
