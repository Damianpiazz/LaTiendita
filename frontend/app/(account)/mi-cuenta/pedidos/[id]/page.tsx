import type { Metadata } from "next";
import Link from "next/link";

import { AccountPageShell } from "@/features/customer/components/account-page-shell";
import { OrderStatusBadge } from "@/features/customer/components/order-status-badge";
import { getCustomerOrder } from "@/features/order/services/order.service";
import { mapOrderStatus } from "@/lib/customers/profile";
import { formatPrice } from "@/features/product/helpers/format-price";

export const metadata: Metadata = {
  title: "Detalle del pedido",
};

type PageProps = {
  params: Promise<{ id: string }>;
};

function displayDate(value: string | null | undefined): string {
  if (!value) return "—";

  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat("es-AR", { dateStyle: "medium" }).format(date);
}

export default async function OrderDetailPage({ params }: PageProps) {
  const { id } = await params;
  const order = await getCustomerOrder(id);

  return (
    <AccountPageShell
      title={`Pedido ${order?.orderNumber ?? `#${id}`}`}
      description="Detalle de los productos, el estado y el total de esta compra."
    >
      {!order ? (
        <p className="rounded-wf-md border border-wf-line bg-wf-surface p-10 text-center text-sm text-wf-ink-muted">
          No encontramos ese pedido entre tus compras.
        </p>
      ) : (
        <div className="flex flex-col gap-6">
          <div className="grid gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5 sm:grid-cols-3">
            <div>
              <span className="block text-xs text-wf-ink-muted">Fecha</span>
              <span className="text-sm text-wf-ink">{displayDate(order.createdAt)}</span>
            </div>
            <div>
              <span className="block text-xs text-wf-ink-muted">Estado</span>
              <OrderStatusBadge status={mapOrderStatus(order.status)} />
            </div>
            <div>
              <span className="block text-xs text-wf-ink-muted">Total</span>
              <span className="text-sm font-medium text-wf-ink">{formatPrice(order.total)}</span>
            </div>
          </div>

          {order.cancelReason ? (
            <p className="rounded-wf-md border border-wf-line bg-wf-surface p-5 text-sm text-wf-ink-secondary">
              Motivo de cancelación: {order.cancelReason}
            </p>
          ) : null}

          <div className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
            <div className="flex h-10 items-center border-b border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted">
              <span className="flex-1">Producto</span>
              <span className="w-20 text-center">Cant.</span>
              <span className="w-32 text-right">Subtotal</span>
            </div>
            {order.items.length === 0 ? (
              <p className="p-5 text-sm text-wf-ink-muted">Sin productos registrados.</p>
            ) : (
              order.items.map((item) => (
                <div
                  key={`${item.productId}-${item.sku ?? ""}`}
                  className="flex items-center gap-4 border-b border-wf-line px-5 py-4 text-sm last:border-b-0"
                >
                  <span className="min-w-0 flex-1 text-wf-ink">{item.productTitle}</span>
                  <span className="w-20 text-center text-wf-ink-muted">{item.quantity}</span>
                  <span className="w-32 text-right font-medium text-wf-ink">
                    {formatPrice(item.subtotal)}
                  </span>
                </div>
              ))
            )}
          </div>

          <Link
            href="/mi-cuenta/pedidos"
            className="self-start text-sm font-medium text-wf-ink-secondary underline-offset-2 hover:text-wf-ink hover:underline"
          >
            ← Volver a mis pedidos
          </Link>
        </div>
      )}
    </AccountPageShell>
  );
}
