import Link from "next/link";

import { formatPrice } from "@/features/product/helpers/format-price";

type OrderSummaryAction = {
  label: string;
  href: string;
};

type OrderSummaryProps = {
  /** Units in the cart; the screen labels them as products. */
  units: number;
  subtotal: number;
  /**
   * Shipping row. Omitted by the shipping step, which shows no shipping
   * amount; the cart passes `A calcular`, which is not an amount yet.
   */
  shipping?: { label: string; value: string; muted?: boolean };
  total: number;
  primaryAction: OrderSummaryAction;
  secondaryAction: OrderSummaryAction;
};

/**
 * Order summary card shared by the checkout steps.
 *
 * Wireframe: 376 wide, 20px padding, 16px gap, radius 12, white with a
 * `border/default` hairline; rows put the label left and the value right.
 */
export function OrderSummary({
  units,
  subtotal,
  shipping,
  total,
  primaryAction,
  secondaryAction,
}: OrderSummaryProps) {
  const productLabel = units === 1 ? "producto" : "productos";

  return (
    <div className="flex w-full flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5 lg:w-[376px] lg:shrink-0 lg:self-start">
      <h2 className="text-base font-bold text-wf-ink">Resumen del pedido</h2>

      <div className="flex items-center justify-between text-sm text-wf-ink-muted">
        <span>
          Subtotal ({units} {productLabel})
        </span>
        <span className="text-wf-ink">{formatPrice(subtotal)}</span>
      </div>

      {shipping ? (
        <div className="flex items-center justify-between text-sm text-wf-ink-muted">
          <span>{shipping.label}</span>
          <span className={shipping.muted ? "text-wf-ink-muted" : "text-wf-ink"}>
            {shipping.value}
          </span>
        </div>
      ) : null}

      <div aria-hidden className="h-px bg-wf-line" />

      <div className="flex items-center justify-between">
        <span className="text-base font-bold text-wf-ink">Total</span>
        <span className="text-lg font-bold text-wf-ink">{formatPrice(total)}</span>
      </div>

      <Link
        href={primaryAction.href}
        className="flex h-12 items-center justify-center rounded-wf-sm bg-wf-inverse text-base font-medium text-wf-on-inverse transition-colors hover:bg-wf-ink"
      >
        {primaryAction.label}
      </Link>

      <Link
        href={secondaryAction.href}
        className="flex h-12 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-base font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
      >
        {secondaryAction.label}
      </Link>

      <p className="text-xs text-wf-ink-muted">Impuestos incluidos en los precios</p>
    </div>
  );
}
