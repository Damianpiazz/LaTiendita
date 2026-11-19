import { create } from "zustand";

import type { CartItem } from "@/features/cart/types";
import { seedCartItems } from "@/mocks/data/cart";

type CartStore = {
  items: CartItem[];
  addItem: (productId: string, quantity?: number) => void;
  /** Sets the units of an existing line; it never drops below one. */
  setQuantity: (productId: string, quantity: number) => void;
  removeItem: (productId: string) => void;
  clear: () => void;
};

/**
 * Local cart state.
 *
 * It starts from the mock seed so the wireframe shows the badge with units
 * before any cart service exists. Adding, updating or removing items only
 * touches this store.
 */
export const useCartStore = create<CartStore>((set) => ({
  items: seedCartItems,
  addItem: (productId, quantity = 1) =>
    set((state) => {
      const units = Math.max(1, Math.trunc(quantity));
      const currentItem = state.items.find((item) => item.productId === productId);

      if (!currentItem) {
        return { items: [...state.items, { productId, quantity: units }] };
      }

      return {
        items: state.items.map((item) =>
          item.productId === productId
            ? { ...item, quantity: item.quantity + units }
            : item,
        ),
      };
    }),
  setQuantity: (productId, quantity) =>
    set((state) => ({
      items: state.items.map((item) =>
        item.productId === productId
          ? { ...item, quantity: Math.max(1, Math.trunc(quantity)) }
          : item,
      ),
    })),
  removeItem: (productId) =>
    set((state) => ({
      items: state.items.filter((item) => item.productId !== productId),
    })),
  clear: () => set({ items: [] }),
}));

export function selectCartCount(state: CartStore): number {
  return state.items.reduce((total, item) => total + item.quantity, 0);
}
