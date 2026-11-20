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
};

/**
 * `GET /admin/categories` seed.
 *
 * The total products line is derived from the seeded counts, so adding a
 * category keeps the header and footer copy honest.
 */
export type AdminCategories = {
  items: AdminCategory[];
  /** Row the detail card shows. */
  selectedId: string;
  /** e.g. `hoy 09:12`; display string because the mock has no clock. */
  updatedLabel: string;
};
