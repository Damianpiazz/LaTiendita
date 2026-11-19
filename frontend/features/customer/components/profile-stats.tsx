import { Fragment } from "react";

import type { CustomerStats } from "@/features/customer/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Stat strip under the profile card: four figures separated by hairline
 * dividers.
 *
 * The frame draws one 81px card with 20/24 padding, so the four stats share the
 * inner width through `grow`. Below `xl` the strip falls back to two columns and
 * the dividers are dropped, since the vertical rules no longer have a row to
 * separate.
 */
export function ProfileStats({ stats }: { stats: CustomerStats }) {
  const figures = [
    { label: "Pedidos", value: String(stats.orders) },
    { label: "Total gastado", value: formatPrice(stats.totalSpent) },
    { label: "Última compra", value: stats.lastPurchase },
    { label: "Reseñas", value: String(stats.reviews) },
  ];

  return (
    <section className="grid grid-cols-2 gap-6 rounded-wf-md border border-wf-line bg-wf-surface px-6 py-5 xl:flex xl:items-center">
      {figures.map((figure, index) => (
        <Fragment key={figure.label}>
          {index > 0 ? (
            <span
              aria-hidden
              className="hidden h-10 w-px shrink-0 bg-wf-line xl:block"
            />
          ) : null}
          <div className="flex flex-col gap-1 xl:grow">
            <span className="text-xs leading-[15px] text-wf-ink-muted">{figure.label}</span>
            <span className="text-lg leading-[22px] font-bold text-wf-ink">{figure.value}</span>
          </div>
        </Fragment>
      ))}
    </section>
  );
}
