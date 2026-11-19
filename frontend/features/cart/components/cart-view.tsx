"use client";

import type { Product } from "@/features/product/types";

import { OrderSummary } from "@/features/checkout";
import {
  buildCartLines,
  countUnits,
  sumSubtotal,
} from "../helpers/cart-totals";
import { useCartStore } from "../store/cart-store";
import { CartEmpty } from "./cart-empty";
import { CartItemRow } from "./cart-item-row";

/**
 * Title row, lines and summary of the cart screen.
 *
 * It is a client component because the cart lives in the store: quantities,
 * removals and totals all come from the same state the header badge reads.
 */
export function CartView({ products }: { products: Product[] }) {
  const items = useCartStore((state) => state.items);
  const lines = buildCartLines(items, products);
  const units = countUnits(lines);
  const subtotal = sumSubtotal(lines);

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-wf-ink">Tu carrito</h1>
          <p className="text-sm text-wf-ink-muted">
            Revisá tus productos. El método de envío se elige en el paso siguiente.
          </p>
        </div>
        <span className="text-sm text-wf-ink-muted">
          {units} {units === 1 ? "producto" : "productos"}
        </span>
      </div>

      {lines.length === 0 ? (
        <CartEmpty />
      ) : (
        <div className="flex flex-col gap-6 lg:flex-row">
          <ul className="flex min-w-0 flex-1 flex-col gap-4">
            {lines.map((line) => (
              <CartItemRow key={line.product.id} line={line} />
            ))}
          </ul>

          <OrderSummary
            units={units}
            subtotal={subtotal}
            shipping={{ label: "Envío", value: "A calcular", muted: true }}
            total={subtotal}
            primaryAction={{ label: "Continuar al envío", href: "/checkout/envio" }}
            secondaryAction={{ label: "Seguir comprando", href: "/" }}
          />
        </div>
      )}
    </>
  );
}
