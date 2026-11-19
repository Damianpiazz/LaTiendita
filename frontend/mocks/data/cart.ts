import type { CartItem } from "@/features/cart/types";

/**
 * Mock cart state used while there is no cart service.
 *
 * It reproduces the state the `Cart Wireframe` frame draws: three lines, four
 * units and a `$ 56.279` subtotal.
 */
export const seedCartItems: CartItem[] = [
  { productId: "auriculares-inalambricos", quantity: 1 },
  { productId: "remera-basica-de-algodon", quantity: 2 },
  { productId: "botella-termica-750-ml", quantity: 1 },
];
