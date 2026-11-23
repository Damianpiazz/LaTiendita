import Link from "next/link";

import { OrderStatusBadge } from "@/features/customer/components/order-status-badge";
import type { CustomerOrder } from "@/features/customer/types";
import { formatPrice } from "@/features/product/helpers/format-price";
import { cn } from "@/lib/utils";

/**
 * `Mis compras`: the 40px table head and the three 56px rows of the frame.
 *
 * Column widths are the frame's (160 / 200 / 200 / 180 plus the growing action)
 * and the wrapper scrolls sideways under 1080px, like the admin tables.
 */
export function ProfilePurchases({ orders }: { orders: CustomerOrder[] }) {
  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-2xl leading-[29px] font-bold text-wf-ink">Mis compras</h2>
        <Link
          href="/mi-cuenta/pedidos"
          className="shrink-0 text-sm leading-[17px] text-wf-ink-secondary underline-offset-2 hover:text-wf-ink hover:underline"
        >
          Ver todos los pedidos →
        </Link>
      </div>

      <div className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
        <div className="overflow-x-auto">
          <div className="min-w-[1080px]">
            <div className="flex h-10 items-center border-b border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted">
              <span className="w-[160px]">Pedido</span>
              <span className="w-[200px]">Fecha</span>
              <span className="w-[200px]">Estado</span>
              <span className="w-[180px]">Total</span>
              <span className="flex-1 text-right">Acción</span>
            </div>

            {orders.length === 0 ? (
              <div className="flex h-16 items-center justify-center text-sm text-wf-ink-muted">
                Todavía no realizaste compras.
              </div>
            ) : null}
            {orders.map((order, index) => (
              <div
                key={order.id}
                className={cn(
                  "flex h-14 items-center px-5 text-sm",
                  index < orders.length - 1 && "border-b border-wf-line",
                )}
              >
                <span className="w-[160px] font-medium text-wf-ink">{order.id}</span>
                <span className="w-[200px] text-wf-ink-muted">{order.date}</span>
                <span className="w-[200px]">
                  <OrderStatusBadge status={order.status} />
                </span>
                <span className="w-[180px] font-medium text-wf-ink">
                  {formatPrice(order.total)}
                </span>
                <span className="flex-1 text-right text-wf-ink-secondary">
                  <Link
                    href={`/mi-cuenta/pedidos/${encodeURIComponent(order.orderId ?? order.id.replace("#", ""))}`}
                    className="underline-offset-2 hover:text-wf-ink hover:underline"
                  >
                    Ver detalle →
                  </Link>
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
