import Link from "next/link";

import type { AdminLowStockProduct } from "@/features/admin/types";

/**
 * Low-stock card of the side column: header plus five 56px rows.
 *
 * The rows are the seeded products under the threshold; the count of the sub
 * line is the list length, so adding a row keeps the copy honest.
 */
export function LowStockCard({
  products,
  threshold,
}: {
  products: AdminLowStockProduct[];
  threshold: number;
}) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="flex items-center justify-between gap-3 p-5">
        <div className="flex flex-col gap-1">
          <h2 className="text-base font-bold text-wf-ink">Stock bajo</h2>
          <p className="text-xs text-wf-ink-muted">
            {products.length} productos con menos de {threshold} u.
          </p>
        </div>
        <Link
          href="/admin/productos"
          className="text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
        >
          Reponer →
        </Link>
      </div>

      <ul className="flex flex-col">
        {products.map((product) => (
          <li
            key={product.sku}
            className="flex h-14 items-center gap-3 border-b border-wf-line px-5"
          >
            <div className="flex min-w-0 flex-1 flex-col gap-1">
              <span className="truncate text-sm font-medium text-wf-ink">
                {product.name}
              </span>
              <span className="text-xs text-wf-ink-muted">{product.sku}</span>
            </div>
            <span className="flex h-6 min-w-9 shrink-0 items-center justify-center rounded-full bg-wf-subtle px-2 text-xs font-bold text-wf-ink-secondary">
              {product.quantity} u.
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
