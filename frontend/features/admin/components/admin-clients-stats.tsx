import { Fragment } from "react";

import type { AdminClients } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Stats strip of the clients screen: four figures separated by hairline
 * dividers, all derived from the store totals.
 *
 * The frame draws this as one 81px card with 20/24 padding, so the four stats
 * share the inner width through `grow` instead of fixed widths. Below `xl` the
 * strip falls back to two columns and the dividers are dropped, since the
 * vertical rules no longer have a row to separate.
 */
export function AdminClientsStats({ clients }: { clients: AdminClients }) {
  const stats = [
    { label: "Clientes totales", value: String(clients.totalClients) },
    { label: "Nuevos este mes", value: String(clients.newThisMonth) },
    { label: "Recurrentes", value: String(clients.recurring) },
    { label: "Ticket promedio", value: formatPrice(clients.averageTicket) },
  ];

  return (
    <section className="grid grid-cols-2 gap-6 rounded-wf-md border border-wf-line bg-wf-surface px-6 py-5 xl:flex xl:items-center">
      {stats.map((stat, index) => (
        <Fragment key={stat.label}>
          {index > 0 ? (
            <span
              aria-hidden
              className="hidden h-10 w-px shrink-0 bg-wf-line xl:block"
            />
          ) : null}
          <div className="flex flex-col gap-1 xl:grow">
            <span className="text-xs text-wf-ink-muted">{stat.label}</span>
            <span className="text-lg font-bold text-wf-ink">{stat.value}</span>
          </div>
        </Fragment>
      ))}
    </section>
  );
}
