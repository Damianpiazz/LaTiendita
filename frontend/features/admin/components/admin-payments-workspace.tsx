"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import { listAdminPayments } from "@/features/admin/actions/payment-actions";
import type {
  AdminPaymentStatusFilter,
  AdminPayments,
  AdminPaymentsQuery,
} from "@/features/admin/types";

import { AdminPagination } from "./admin-pagination";
import { AdminPaymentsFilters } from "./admin-payments-filters";
import { AdminPaymentsHeader } from "./admin-payments-header";
import { AdminPaymentsKpis } from "./admin-payments-kpis";
import { AdminPaymentsTable } from "./admin-payments-table";

const PAGE_SIZE = 8;
const DEFAULT_SORT = "paidAt,desc";
const LOAD_ERROR = "No se pudo cargar la lista de pagos.";

type AdminPaymentsWorkspaceProps = {
  initialPayments: AdminPayments;
};

/**
 * Interactive payments screen. The server seeds the first page; search,
 * status pills, sort and pagination refetch through the same server action.
 */
export function AdminPaymentsWorkspace({
  initialPayments,
}: AdminPaymentsWorkspaceProps) {
  const [payments, setPayments] = useState(initialPayments);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<AdminPaymentStatusFilter>("todos");
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
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
    };
  }, [page, payments.pageSize, search, sort, status]);

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
        onStatusChange={(value) => resetPage(setStatus, value)}
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
      />
      <AdminPagination
        label={`Mostrando ${firstItem}–${lastItem} de ${payments.totalPayments} pagos`}
        pageCount={pageCount}
        current={page}
        onPageChange={setPage}
      />
    </div>
  );
}
