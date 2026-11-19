export type Product = {
  id: string;
  slug: string;
  name: string;
  /** Price in ARS, without decimals. */
  price: number;
  categoryId: string;
  /** Category label as returned by the catalog API, ready to render. */
  categoryName: string;
  /** Group inside the category, e.g. `Audio` under `Tecnología`. */
  subcategory: string;
  featured: boolean;
};
