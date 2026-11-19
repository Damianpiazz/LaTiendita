/** Product states the admin table renders as pills. */
export type AdminProductStatus = "publicado" | "borrador" | "agotado";

/** One row of the admin products table. */
export type AdminProduct = {
  /** SKU, the stable identity of the row. */
  id: string;
  name: string;
  sku: string;
  category: string;
  price: number;
  stock: number;
  status: AdminProductStatus;
  /** Marks the row checked and feeds the bulk bar count. */
  selected?: boolean;
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
