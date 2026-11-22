/** One row of the admin categories card. */
export type AdminCategory = {
  id: string;
  name: string;
  /** URL segment, e.g. `remeras`; the printed slug is `/categorias/remeras`. */
  slug: string;
  description: string;
  productCount: number;
  lowStockCount: number;
  /** Whether the category is shown in the Home wireframe grid. */
  featured: boolean;
  /** Visibility in the store. Older rows omit it and are treated as active. */
  active?: boolean;
  /** Image URL; the wireframe keeps `[ Imagen ]` while it is empty. */
  imageUrl?: string | null;
};

/**
 * Page returned by `GET /api/categories` plus the row the detail card opens.
 *
 * The header and footer counts are derived from the visible rows, so the copy
 * follows the seed as well as the real page.
 */
export type AdminCategories = {
  items: AdminCategory[];
  /** Row the detail card shows. */
  selectedId: string;
  /** e.g. `hoy 09:12`; display string because the mock has no clock. */
  updatedLabel: string;
  /** Total rows reported by the backend page, when available. */
  total?: number;
};

/** Filters accepted by the categories adapter and its list action. */
export type AdminCategoriesQuery = {
  q?: string;
  featured?: boolean;
  page?: number;
  size?: number;
  sort?: string;
};

/** Payload of the category form; the backend DTO is built at the action boundary. */
export type AdminCategoryPayload = {
  categoryId?: string;
  name: string;
  slug: string;
  description?: string;
  featured: boolean;
  active: boolean;
  imageUrl?: string | null;
};

/** Deletion either succeeds or reports the 409 guard as observable UI state. */
export type AdminCategoryDeleteResult =
  | { ok: true }
  | { ok: false; error: string };
