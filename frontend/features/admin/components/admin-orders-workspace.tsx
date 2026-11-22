"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import { listAdminOrders } from "@/features/admin/actions/order-actions";
import { AdminOrdersFilters } from "@/features/admin/components/admin-orders-filters";
import { AdminOrdersHeader } from "@/features/admin/components/admin-orders-header";
import { AdminOrdersTable } from "@/features/admin/components/admin-orders-table";
import { AdminPagination } from "@/features/admin/components/admin-pagination";
import type {
  AdminOrderStatusFilter,
  AdminOrders,
  AdminOrdersQuery,
} from "@/features/admin/types";

const ADMIN_ORDERS_PAGE_SIZE = 8;
const ADMIN_ORDERS_DEFAULT_SORT = "createdAt,desc";

type Props = {
  initialOrders: AdminOrders;
};

export function AdminOrdersWorkspace({ initialOrders }: Props) {
  const [orders, setOrders] = useState<AdminOrders>(initialOrders);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState<AdminOrderStatusFilter>("all");
  const [page, setPage] = useState(1);
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
        sort: ADMIN_ORDERS_DEFAULT_SORT,
      });
    }, q ? 300 : 0);
    return () => clearTimeout(timer);
  }, [q, status, page, fetchOrders]);

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
        onStatusChange={(next) => {
          setStatus(next);
          setPage(1);
        }}
      />
      <AdminOrdersTable
        orders={orders}
        q={q}
        loading={loading}
        loadError={loadError}
        onSearchChange={(value) => {
          setQ(value);
          setPage(1);
        }}
      />
      <AdminPagination
        label={`Mostrando ${first}–${last} de ${orders.totalOrders} pedidos`}
        pageCount={pageCount}
        current={page}
        onPageChange={setPage}
      />
    </div>
  );
}