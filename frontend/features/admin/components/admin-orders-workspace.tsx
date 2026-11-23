"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import { listAdminOrders } from "@/features/admin/actions/order-actions";
import { AdminDetailDrawer, AdminDetailRow } from "@/features/admin/components/admin-detail-drawer";
import { AdminOrdersFilters } from "@/features/admin/components/admin-orders-filters";
import { AdminOrdersHeader } from "@/features/admin/components/admin-orders-header";
import { AdminOrdersTable } from "@/features/admin/components/admin-orders-table";
import { AdminPagination } from "@/features/admin/components/admin-pagination";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { ADMIN_RANGE_DEFAULT_DAYS } from "@/features/admin/helpers/admin-range";
import type {
  AdminOrder,
  AdminOrderStatusFilter,
  AdminOrders,
  AdminOrdersQuery,
  AdminPaymentMethod,
} from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

const ADMIN_ORDERS_PAGE_SIZE = 8;
const ADMIN_ORDERS_DEFAULT_SORT = "createdAt,desc";

const PAYMENT_LABELS: Record<AdminPaymentMethod, string> = {
  "mercado-pago": "Mercado Pago",
  transferencia: "Transferencia",
  efectivo: "Efectivo",
  "—": "—",
};

type InitialQuery = { q?: string; days?: number };

type Props = {
  initialOrders: AdminOrders;
  /** Parsed from the URL so the topbar search and range links reach this table. */
  initialQuery?: InitialQuery;
};

export function AdminOrdersWorkspace({ initialOrders, initialQuery = {} }: Props) {
  const [orders, setOrders] = useState<AdminOrders>(initialOrders);
  const [q, setQ] = useState(initialQuery.q ?? "");
  const [days, setDays] = useState(initialQuery.days ?? ADMIN_RANGE_DEFAULT_DAYS);
  const [status, setStatus] = useState<AdminOrderStatusFilter>("all");
  const [sort, setSort] = useState(ADMIN_ORDERS_DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [selectedOrder, setSelectedOrder] = useState<AdminOrder | null>(null);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const requestId = useRef(0);
  const didMount = useRef(false);

  const fetchOrders = useCallback(async (next: AdminOrdersQuery) => {
    const id = ++requestId.current;
    setLoading(true);
    setLoadError(null);
    try {
      const result = await listAdminOrders(next);
      if (id !== requestId.current) return;
      setOrders(result);
    } catch {
      if (id !== requestId.current) return;
      setLoadError("No se pudieron cargar los pedidos.");
    } finally {
      if (id === requestId.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!didMount.current) {
      didMount.current = true;
      return;
    }
    const timer = setTimeout(() => {
      void fetchOrders({
        q: q.trim() || undefined,
        status,
        page,
        size: ADMIN_ORDERS_PAGE_SIZE,
        sort,
        days,
      });
    }, q ? 300 : 0);
    return () => clearTimeout(timer);
  }, [q, status, page, sort, days, fetchOrders]);

  const pageSize = orders.pageSize || ADMIN_ORDERS_PAGE_SIZE;
  const pageCount = Math.max(1, Math.ceil(orders.totalOrders / pageSize));
  const first = orders.items.length ? (page - 1) * pageSize + 1 : 0;
  const last = orders.items.length ? first + orders.items.length - 1 : 0;

  return (
    <div className="flex flex-col gap-6">
      <AdminOrdersHeader orders={orders} />
      <AdminOrdersFilters
        orders={orders}
        status={status}
        days={days}
        onStatusChange={(next) => {
          setStatus(next);
          setPage(1);
        }}
        onDaysChange={(next) => {
          setDays(next);
          setPage(1);
        }}
      />
      <AdminOrdersTable
        orders={orders}
        q={q}
        sort={sort}
        loading={loading}
        loadError={loadError}
        onSearchChange={(value) => {
          setQ(value);
          setPage(1);
        }}
        onSortChange={(value) => {
          setSort(value);
          setPage(1);
        }}
        onSelect={setSelectedOrder}
      />
      <AdminPagination
        label={`Mostrando ${first}–${last} de ${orders.totalOrders} pedidos`}
        pageCount={pageCount}
        current={page}
        onPageChange={setPage}
      />

      {selectedOrder ? (
        <AdminDetailDrawer
          title={`Pedido ${selectedOrder.id}`}
          onClose={() => setSelectedOrder(null)}
        >
          <dl className="flex flex-col gap-3">
            <AdminDetailRow label="Cliente">{selectedOrder.customer}</AdminDetailRow>
            <AdminDetailRow label="Email">{selectedOrder.email}</AdminDetailRow>
            <AdminDetailRow label="Fecha">{selectedOrder.placedAt}</AdminDetailRow>
            <AdminDetailRow label="Artículos">
              {selectedOrder.itemCount} artículo{selectedOrder.itemCount === 1 ? "" : "s"}
            </AdminDetailRow>
            <AdminDetailRow label="Pago">
              {PAYMENT_LABELS[selectedOrder.payment]}
            </AdminDetailRow>
            <AdminDetailRow label="Total">{formatPrice(selectedOrder.total)}</AdminDetailRow>
            <AdminDetailRow label="Estado">
              <StatusBadge status={selectedOrder.status} />
            </AdminDetailRow>
          </dl>
        </AdminDetailDrawer>
      ) : null}
    </div>
  );
}
