"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import {
  exportAdminClients,
  listAdminClients,
} from "@/features/admin/actions/client-actions";
import { AdminClientDetailDialog } from "@/features/admin/components/admin-client-detail-dialog";
import { AdminClientsHeader } from "@/features/admin/components/admin-clients-header";
import { AdminClientsStats } from "@/features/admin/components/admin-clients-stats";
import { AdminClientsTable } from "@/features/admin/components/admin-clients-table";
import { AdminClientsToolbar } from "@/features/admin/components/admin-clients-toolbar";
import { AdminPagination } from "@/features/admin/components/admin-pagination";
import {
  ADMIN_CLIENTS_DEFAULT_SORT,
  ADMIN_CLIENTS_PAGE_SIZE,
} from "@/features/admin/helpers/admin-clients.constants";
import type {
  AdminClientDetail,
  AdminClientSegment,
  AdminClients,
  AdminClientsQuery,
} from "@/features/admin/types";

const SEGMENT_OPTIONS: { label: string; value: AdminClientSegment | undefined }[] = [
  { label: "Todos", value: undefined },
  { label: "Nuevos", value: "nueva" },
  { label: "Recurrentes", value: "recurrente" },
  { label: "Sin compras", value: "sin-compras" },
];

export type AdminClientsWorkspaceProps = {
  initialClients: AdminClients;
  initialQuery?: AdminClientsQuery;
  /** Loaded by the server page when the URL carries `?customer=...`. */
  detail?: AdminClientDetail | null;
};

/**
 * Interactive clients screen. The server seeds the first page and the detail;
 * every filter, page change and export goes through the client server actions.
 */
export function AdminClientsWorkspace({
  initialClients,
  initialQuery = {},
  detail = null,
}: AdminClientsWorkspaceProps) {
  const [clients, setClients] = useState(initialClients);
  const [search, setSearch] = useState(initialQuery.name ?? "");
  const [segment, setSegment] = useState<AdminClientSegment | undefined>(
    initialQuery.segment,
  );
  const [sort, setSort] = useState(
    initialQuery.sort ?? ADMIN_CLIENTS_DEFAULT_SORT,
  );
  const [page, setPage] = useState(initialQuery.page ?? 1);
  const [menuOpen, setMenuOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const requestId = useRef(0);

  const size = initialQuery.size ?? initialClients.pageSize ?? ADMIN_CLIENTS_PAGE_SIZE;

  const query = useMemo<AdminClientsQuery>(() => {
    const trimmed = search.trim();

    return {
      ...(trimmed ? { name: trimmed, email: trimmed, phone: trimmed } : {}),
      ...(segment ? { segment } : {}),
      ...(initialQuery.status ? { status: initialQuery.status } : {}),
      page,
      size,
      sort,
    };
  }, [initialQuery.status, page, search, segment, size, sort]);

  useEffect(() => {
    const current = ++requestId.current;
    void listAdminClients(query)
      .then((next) => {
        if (current === requestId.current) {
          setClients(next);
          setLoadError(null);
        }
      })
      .catch(() => {
        if (current === requestId.current) {
          setLoadError("No se pudieron cargar los clientes.");
        }
      });
  }, [query]);

  function updateFilter<T>(setter: (value: T) => void, value: T) {
    setter(value);
    setPage(1);
  }

  function toggleSort() {
    setSort((current) =>
      current.endsWith(",asc")
        ? current.replace(/,asc$/, ",desc")
        : current.replace(/,desc$/, ",asc"),
    );
    setPage(1);
  }

  async function handleExport() {
    setExporting(true);
    try {
      const { filename, content } = await exportAdminClients(query);
      const url = URL.createObjectURL(
        new Blob([content], { type: "text/csv;charset=utf-8" }),
      );
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      setLoadError(null);
    } catch {
      setLoadError("No se pudo exportar el CSV.");
    } finally {
      setExporting(false);
    }
  }

  const pageSize = clients.pageSize || ADMIN_CLIENTS_PAGE_SIZE;
  const pageCount = Math.max(1, Math.ceil(clients.totalClients / pageSize));
  const first = clients.items.length ? (page - 1) * pageSize + 1 : 0;
  const last = clients.items.length ? first + clients.items.length - 1 : 0;

  return (
    <div className="flex flex-col gap-6">
      <AdminClientsHeader
        clients={clients}
        onExport={() => void handleExport()}
        onSegments={() => setMenuOpen((open) => !open)}
        segmentsOpen={menuOpen}
        exporting={exporting}
      />

      {menuOpen ? (
        <div
          role="menu"
          aria-label="Segmentos"
          className="flex flex-wrap gap-2 rounded-wf-sm border border-wf-line bg-wf-surface p-2"
        >
          {SEGMENT_OPTIONS.map((option) => (
            <button
              key={option.label}
              type="button"
              role="menuitem"
              onClick={() => {
                updateFilter(setSegment, option.value);
                setMenuOpen(false);
              }}
              className="flex h-9 items-center rounded-wf-sm px-3 text-sm text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
            >
              {option.label}
            </button>
          ))}
        </div>
      ) : null}

      <AdminClientsStats clients={clients} />
      <AdminClientsToolbar
        search={search}
        segment={segment}
        sort={sort}
        onSearchChange={(value) => updateFilter(setSearch, value)}
        onSegmentChange={(value) => updateFilter(setSegment, value)}
        onSortChange={toggleSort}
      />
      {loadError ? (
        <p role="alert" className="text-sm text-wf-ink-secondary">
          {loadError}
        </p>
      ) : null}
      <AdminClientsTable clients={clients} />
      <AdminPagination
        label={`Mostrando ${first}–${last} de ${clients.totalClients} clientes`}
        pageCount={pageCount}
        current={page}
        onPageChange={setPage}
      />
      {detail ? <AdminClientDetailDialog detail={detail} /> : null}
    </div>
  );
}
