"use client";

import Link from "next/link";
import { useId } from "react";

import { ClientStatusBadge } from "@/features/admin/components/client-status-badge";
import type { AdminClientDetail } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

/**
 * Detail surfaced by `Ver →`. The data comes from the server page query param,
 * so no customer token is ever passed to this client component.
 */
export function AdminClientDetailDialog({
  detail,
}: {
  detail: AdminClientDetail;
}) {
  const titleId = useId();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div aria-hidden className="absolute inset-0 bg-wf-inverse/45" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className="relative flex max-h-[calc(100vh-2rem)] w-full max-w-[480px] flex-col overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface"
      >
        <div className="min-h-0 flex-1 overflow-y-auto p-8">
          <div className="flex flex-col gap-6">
            <div className="flex items-start justify-between gap-4">
              <div className="flex flex-col gap-1">
                <h2 id={titleId} className="text-2xl font-bold text-wf-ink">
                  {detail.name}
                </h2>
                <p className="text-sm text-wf-ink-muted">{detail.email}</p>
              </div>
              <ClientStatusBadge status={detail.status} />
            </div>

            <dl className="grid grid-cols-2 gap-4 rounded-wf-sm bg-wf-canvas p-4 text-sm">
              <div className="flex flex-col gap-1">
                <dt className="text-xs text-wf-ink-muted">Teléfono</dt>
                <dd className="font-medium text-wf-ink">{detail.phone}</dd>
              </div>
              <div className="flex flex-col gap-1">
                <dt className="text-xs text-wf-ink-muted">Última compra</dt>
                <dd className="font-medium text-wf-ink">
                  {detail.lastPurchase}
                </dd>
              </div>
              <div className="flex flex-col gap-1">
                <dt className="text-xs text-wf-ink-muted">Pedidos</dt>
                <dd className="font-medium text-wf-ink">{detail.orderCount}</dd>
              </div>
              <div className="flex flex-col gap-1">
                <dt className="text-xs text-wf-ink-muted">Total gastado</dt>
                <dd className="font-medium text-wf-ink">
                  {formatPrice(detail.totalSpent)}
                </dd>
              </div>
            </dl>
          </div>
        </div>
        <footer className="flex shrink-0 items-center justify-end border-t border-wf-line px-8 py-4">
          <Link
            href="/admin/clientes"
            className="flex h-10 min-w-[120px] items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
          >
            Cerrar
          </Link>
        </footer>
      </div>
    </div>
  );
}
