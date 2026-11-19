"use client";

import Link from "next/link";

import { selectCartCount, useCartStore } from "@/features/cart/store/cart-store";

/**
 * Cart control of the header.
 *
 * Wireframe: 40px outlined box, radius 8, `border/strong`; `Carrito` label in
 * `text/secondary` and an 18px inverse badge with the unit count.
 */
export function CartBadge() {
  const count = useCartStore(selectCartCount);

  return (
    <Link
      href="/carrito"
      className="inline-flex h-10 shrink-0 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3.5 text-sm text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
      aria-label={`Carrito, ${count} ${count === 1 ? "unidad" : "unidades"}`}
    >
      <span aria-hidden>Carrito</span>
      <span
        aria-hidden
        className="inline-flex size-[18px] items-center justify-center rounded-full bg-wf-inverse text-[10px] font-bold leading-none text-wf-on-inverse"
      >
        {count}
      </span>
    </Link>
  );
}
