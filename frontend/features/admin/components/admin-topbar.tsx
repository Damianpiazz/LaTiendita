"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

import type { AdminNavigationCounts } from "@/features/admin/types";
import { sessionInitials, type SessionUser } from "@/lib/auth/session";

/**
 * Topbar of the admin shell, measured from the `Topbar` frame: 72 tall, 32px
 * gutters, white with a bottom hairline. Search opens the orders table with the
 * typed term; `Avisos` lists the live navigation counts as links.
 */
export function AdminTopbar({
  counts,
  user,
}: {
  counts: AdminNavigationCounts;
  user: SessionUser | null;
}) {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [alertsOpen, setAlertsOpen] = useState(false);

  function submitSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const q = search.trim();
    if (q) router.push(`/admin/pedidos?q=${encodeURIComponent(q)}`);
  }

  const notices = [
    {
      label: `${counts.pendingOrders} pedidos pendientes de despacho`,
      href: "/admin/pedidos",
    },
    { label: `${counts.payments} pagos por revisar`, href: "/admin/pagos" },
    { label: `${counts.alerts} alertas de catálogo`, href: "/admin/productos" },
  ];

  return (
    <header className="flex h-[72px] shrink-0 items-center justify-between gap-4 border-b border-wf-line bg-wf-surface px-8">
      <form
        role="search"
        onSubmit={submitSearch}
        className="hidden h-10 w-[320px] items-center rounded-wf-sm border border-wf-line bg-wf-canvas px-4 md:flex"
      >
        <label htmlFor="admin-search" className="sr-only">
          Buscar en la tienda
        </label>
        <input
          id="admin-search"
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Buscar pedidos, productos o clientes..."
          className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
        />
      </form>

      <div className="flex items-center gap-3">
        <div className="relative">
          <button
            type="button"
            aria-haspopup="menu"
            aria-expanded={alertsOpen}
            onClick={() => setAlertsOpen((open) => !open)}
            className="flex h-10 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
          >
            Avisos
            <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-wf-subtle px-2 text-xs font-bold text-wf-ink-secondary">
              {counts.alerts}
            </span>
          </button>
          {alertsOpen ? (
            <div
              role="menu"
              aria-label="Avisos"
              className="absolute right-0 top-12 z-50 flex w-72 flex-col rounded-wf-md border border-wf-line bg-wf-surface p-2 shadow-lg"
            >
              {notices.map((notice) => (
                <Link
                  key={notice.href}
                  href={notice.href}
                  role="menuitem"
                  onClick={() => setAlertsOpen(false)}
                  className="rounded-wf-sm px-3 py-2 text-sm text-wf-ink-secondary hover:bg-wf-canvas hover:text-wf-ink"
                >
                  {notice.label}
                </Link>
              ))}
            </div>
          ) : null}
        </div>

        <span
          className="flex size-10 items-center justify-center rounded-full bg-wf-subtle text-sm font-bold text-wf-ink-secondary"
          title={user ? user.email : "Sin sesión"}
        >
          {user ? sessionInitials(user) : "--"}
        </span>
      </div>
    </header>
  );
}
