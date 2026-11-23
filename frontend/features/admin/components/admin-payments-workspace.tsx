"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import { listAdminPayments } from "@/features/admin/actions/payment-actions";
import { AdminDetailDrawer, AdminDetailRow } from "@/features/admin/components/admin-detail-drawer";
import { ADMIN_RANGE_DEFAULT_DAYS } from "@/features/admin/helpers/admin-range";
import type {
  AdminPayment,
  AdminPaymentStatusFilter,
  AdminPayments,
  AdminPaymentsQuery,
} from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";

import { AdminPagination } from "./admin-pagination";
import { AdminPaymentsFilters } from "./admin-payments-filters";
import { AdminPaymentsHeader } from "./admin-payments-header";
import { AdminPaymentsKpis } from "./admin-payments-kpis";
import { AdminPaymentsTable } from "./admin-payments-table";
import { PaymentStatusBadge } from "./payment-status-badge";

const PAGE_SIZE = 8;
const DEFAULT_SORT = "paidAt,desc";
const DEFAULT_DAYS = ADMIN_RANGE_DEFAULT_DAYS;
const LOAD_ERROR = "No se pudo cargar la lista de pagos.";

type AdminPaymentsWorkspaceProps = {
  initialPayments: AdminPayments;
};

/**
 * Interactive payments screen. The server seeds the first page; search,
 * status pills, date range, sort and pagination refetch through the same
 * server action. Row actions open a detail drawer with the loaded payment.
 */
export function AdminPaymentsWorkspace({
  initialPayments,
}: AdminPaymentsWorkspaceProps) {
  const [payments, setPayments] = useState(initialPayments);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<AdminPaymentStatusFilter>("todos");
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [days, setDays] = useState(DEFAULT_DAYS);
  const [page, setPage] = useState(1);
  const [selectedPayment, setSelectedPayment] = useState<AdminPayment | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  const skipInitialFetch = useRef(true);
  const requestId = useRef(0);

  const query = useMemo<AdminPaymentsQuery>(() => {
    const trimmed = search.trim();

    return {
      ...(trimmed ? { q: trimmed } : {}),
      ...(status !== "all" && status !== "todos" ? { status } : {}),
      page,
      size: payments.pageSize || PAGE_SIZE,
      sort,
      days,
    };
  }, [page, payments.pageSize, search, sort, status, days]);

  useEffect(() => {
    if (skipInitialFetch.current) {
      skipInitialFetch.current = false;
      return;
    }

    const current = ++requestId.current;
    void listAdminPayments(query)
      .then((next) => {
        if (current === requestId.current) {
          setPayments(next);
          setLoadError(null);
        }
      })
      .catch(() => {
        if (current === requestId.current) {
          setLoadError(LOAD_ERROR);
        }
      });
  }, [query]);

  const pageSize = payments.pageSize || PAGE_SIZE;
  const firstItem = payments.totalPayments === 0 ? 0 : (page - 1) * pageSize + 1;
  const lastItem = Math.min(page * pageSize, payments.totalPayments);
  const pageCount = Math.max(1, Math.ceil(payments.totalPayments / pageSize));

  function resetPage<T>(setter: (value: T) => void, value: T) {
    setter(value);
    setPage(1);
  }

  return (
    <div className="flex flex-col gap-6">
      <AdminPaymentsHeader payments={payments} />
      <AdminPaymentsKpis payments={payments} />
      <AdminPaymentsFilters
        payments={payments}
        status={status}
        days={days}
        onStatusChange={(value) => resetPage(setStatus, value)}
        onDaysChange={(value) => resetPage(setDays, value)}
      />
      {loadError ? (
        <p role="alert" className="text-sm text-wf-ink-secondary">
          {loadError}
        </p>
      ) : null}
      <AdminPaymentsTable
        payments={payments}
        search={search}
        sort={sort}
        onSearchChange={(value) => resetPage(setSearch, value)}
        onSortChange={(value) => resetPage(setSort, value)}
        onSelect={setSelectedPayment}
      />
      <AdminPagination
        label={`Mostrando ${firstItem}–${lastItem} de ${payments.totalPayments} pagos`}
        pageCount={pageCount}
        current={page}
        onPageChange={setPage}
      />

      {selectedPayment ? (
        <AdminDetailDrawer
          title={`Pago ${selectedPayment.id}`}
          onClose={() => setSelectedPayment(null)}
        >
          <dl className="flex flex-col gap-3">
            <AdminDetailRow label="Pedido">{selectedPayment.orderId}</AdminDetailRow>
            <AdminDetailRow label="Cliente">{selectedPayment.customer}</AdminDetailRow>
            {selectedPayment.email ? (
              <AdminDetailRow label="Email">{selectedPayment.email}</AdminDetailRow>
            ) : null}
            <AdminDetailRow label="Proveedor">{selectedPayment.provider}</AdminDetailRow>
            <AdminDetailRow label="Método">{selectedPayment.method}</AdminDetailRow>
            <AdminDetailRow label="Fecha">{selectedPayment.paidAt}</AdminDetailRow>
            <AdminDetailRow label="Monto">{formatPrice(selectedPayment.amount)}</AdminDetailRow>
            <AdminDetailRow label="Estado">
              <PaymentStatusBadge status={selectedPayment.status} />
            </AdminDetailRow>
          </dl>
        </AdminDetailDrawer>
      ) : null}
    </div>
  );
}
