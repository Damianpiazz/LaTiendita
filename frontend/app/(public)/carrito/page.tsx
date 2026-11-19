import type { Metadata } from "next";

import { CartPage } from "@/features/cart/pages/cart-page";

export const metadata: Metadata = {
  title: "Tu carrito",
};

export default function Page() {
  return <CartPage />;
}
