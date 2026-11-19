"use client";

import { useState } from "react";

import { useCartStore } from "@/features/cart/store/cart-store";

type PurchasePanelProps = {
  productId: string;
};

/**
 * Quantity control plus the two purchase actions of the detail screen.
 *
 * It renders its three blocks as siblings so the info column keeps the 14px
 * rhythm measured in the wireframe.
 */
export function PurchasePanel({ productId }: PurchasePanelProps) {
  const addItem = useCartStore((state) => state.addItem);
  const [quantity, setQuantity] = useState(1);

  return (
    <>
      <span className="text-[13px] font-medium text-wf-ink">Cantidad</span>

      <div className="flex gap-3">
        <div className="flex h-12 w-[132px] shrink-0 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface">
          <button
            type="button"
            aria-label="Restar una unidad"
            onClick={() => setQuantity((current) => Math.max(1, current - 1))}
            disabled={quantity <= 1}
            className="flex h-full w-10 items-center justify-center text-base text-wf-ink disabled:opacity-40"
          >
            -
          </button>
          <span
            aria-live="polite"
            className="flex-1 text-center text-[15px] font-bold text-wf-ink"
          >
            {quantity}
          </span>
          <button
            type="button"
            aria-label="Sumar una unidad"
            onClick={() => setQuantity((current) => current + 1)}
            className="flex h-full w-10 items-center justify-center text-base text-wf-ink"
          >
            +
          </button>
        </div>

        <button
          type="button"
          onClick={() => addItem(productId, quantity)}
          className="h-12 flex-1 rounded-wf-sm bg-wf-inverse text-[15px] font-bold text-wf-on-inverse transition-colors hover:bg-wf-ink"
        >
          Agregar al carrito
        </button>
      </div>

      <button
        type="button"
        className="h-12 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface text-[15px] font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
      >
        Comprar ahora
      </button>
    </>
  );
}
