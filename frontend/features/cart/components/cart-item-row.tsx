"use client";

import Link from "next/link";

import { ImagePlaceholder } from "@/components/ui/image-placeholder";
import type { CartLine } from "@/features/cart/helpers/cart-totals";
import { useCartStore } from "@/features/cart/store/cart-store";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * One line of the cart list: image, identity, quantity control and line total.
 *
 * Wireframe: 944×128 card, 16px padding and gap, radius 12, white with a
 * `border/default` hairline; the controls column is 120 wide and right aligned.
 */
export function CartItemRow({ line }: { line: CartLine }) {
  const setQuantity = useCartStore((state) => state.setQuantity);
  const removeItem = useCartStore((state) => state.removeItem);
  const { product, quantity, total } = line;

  return (
    <li className="flex gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-4">
      <ImagePlaceholder className="size-24 shrink-0 text-xs" />

      <div className="flex min-w-0 flex-1 flex-col gap-2">
        <span className="text-xs text-wf-ink-muted">{product.categoryName}</span>
        <Link
          href={`/productos/${product.slug}`}
          className="text-base font-bold text-wf-ink transition-colors hover:text-wf-ink-secondary"
        >
          {product.name}
        </Link>
        <span className="text-sm text-wf-ink-muted">
          {formatPrice(product.price)} c/u
        </span>
        <button
          type="button"
          onClick={() => removeItem(product.id)}
          className="self-start text-sm text-wf-ink-muted underline-offset-2 transition-colors hover:text-wf-ink hover:underline"
        >
          Quitar
        </button>
      </div>

      <div className="flex shrink-0 flex-col items-end gap-3">
        <span className="text-lg font-bold text-wf-ink">{formatPrice(total)}</span>

        <div className="flex h-10 w-[120px] items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface">
          <button
            type="button"
            aria-label={`Restar una unidad de ${product.name}`}
            onClick={() => setQuantity(product.id, quantity - 1)}
            disabled={quantity <= 1}
            className="flex size-10 items-center justify-center text-base text-wf-ink-secondary disabled:opacity-40"
          >
            −
          </button>
          <span
            aria-live="polite"
            className="flex size-10 items-center justify-center border-x border-wf-line text-sm font-medium text-wf-ink-secondary"
          >
            {quantity}
          </span>
          <button
            type="button"
            aria-label={`Sumar una unidad de ${product.name}`}
            onClick={() => setQuantity(product.id, quantity + 1)}
            className="flex size-10 items-center justify-center text-base text-wf-ink-secondary"
          >
            +
          </button>
        </div>
      </div>
    </li>
  );
}
