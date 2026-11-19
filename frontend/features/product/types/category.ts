export type Category = {
  id: string;
  slug: string;
  name: string;
  /** Products listed under the category, as the catalog API reports it. */
  productCount: number;
  /** One-line summary shown under the name on the category screen. */
  description?: string;
  /** Featured categories are the ones shown on the home grid. */
  featured: boolean;
};
