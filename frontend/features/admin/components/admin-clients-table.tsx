import { ClientStatusBadge } from "@/features/admin/components/client-status-badge";
import type { AdminClients } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Clients card: the 40px head and eight 64px rows.
 *
 * The frame draws no card header here (unlike products and orders), so the
 * card starts with the table head: 552 = 40 + 8 × 64. Column widths are the
 * frame's and the wrapper scrolls sideways under 1096px.
 */
export function AdminClientsTable({ clients }: { clients: AdminClients }) {
  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="overflow-x-auto">
        <div role="table" aria-label="Clientes" className="min-w-[1096px]">
          <div
            role="row"
            className="flex h-10 items-center border-b border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted"
          >
            <div role="columnheader" className="w-[340px]">
              Cliente
            </div>
            <div role="columnheader" className="w-[150px]">
              Teléfono
            </div>
            <div role="columnheader" className="w-[90px]">
              Pedidos
            </div>
            <div role="columnheader" className="w-[140px]">
              Total gastado
            </div>
            <div role="columnheader" className="w-[140px]">
              Última compra
            </div>
            <div role="columnheader" className="w-[130px]">
              Estado
            </div>
            <div role="columnheader" className="flex-1 text-right">
              Acciones
            </div>
          </div>

          {clients.items.map((client) => (
            <div
              key={client.email}
              role="row"
              className="flex h-16 items-center border-b border-wf-line px-5 text-sm last:border-b-0"
            >
              <div role="cell" className="flex w-[340px] min-w-0 items-center gap-3">
                <span
                  aria-hidden
                  className="flex size-9 shrink-0 items-center justify-center rounded-full bg-wf-subtle text-xs font-bold text-wf-ink-secondary"
                >
                  {client.initials}
                </span>
                <span className="flex min-w-0 flex-col gap-1">
                  <span className="truncate font-medium text-wf-ink">
                    {client.name}
                  </span>
                  <span className="truncate text-xs text-wf-ink-muted">
                    {client.email}
                  </span>
                </span>
              </div>

              <div role="cell" className="w-[150px] text-wf-ink-secondary">
                {client.phone}
              </div>
              <div role="cell" className="w-[90px] text-wf-ink-secondary">
                {client.orderCount}
              </div>
              <div role="cell" className="w-[140px] font-medium text-wf-ink">
                {formatPrice(client.totalSpent)}
              </div>
              <div role="cell" className="w-[140px] text-wf-ink-muted">
                {client.lastPurchase}
              </div>
              <div role="cell" className="w-[130px]">
                <ClientStatusBadge status={client.status} />
              </div>
              <div
                role="cell"
                className="flex flex-1 justify-end font-medium text-wf-ink-secondary"
              >
                Ver →
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
