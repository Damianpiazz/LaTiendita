import type { CartItem } from "@/features/cart/types";
import type { Product } from "@/features/product/types";

export type CartLine = {
  product: Product;
  quantity: number;
  /** Unit price times quantity, in ARS. */
  total: number;
};

/**
 * Joins the stored lines with the catalog.
 *
 * A stored line whose product is no longer in the catalog is dropped instead of
 * rendering an empty row; the store stays as the user left it.
 */
export function buildCartLines(items: CartItem[], products: Product[]): CartLine[] {
  const productsById = new Map(products.map((product) => [product.id, product]));

  return items.flatMap((item) => {
    const product = productsById.get(item.productId);

    if (!product) {
      return [];
    }

    return [
      {
        product,
        quantity: item.quantity,
        total: product.price * item.quantity,
      },
    ];
  });
}

/** Units across every line, which is the number the screen labels as products. */
export function countUnits(lines: CartLine[]): number {
  return lines.reduce((total, line) => total + line.quantity, 0);
}

export function sumSubtotal(lines: CartLine[]): number {
  return lines.reduce((total, line) => total + line.total, 0);
}
