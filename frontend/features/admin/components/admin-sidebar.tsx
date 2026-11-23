"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import {
  adminNavSections,
  type AdminNavItem,
} from "@/features/admin/config/admin-nav";
import type { AdminNavigationCounts } from "@/features/admin/types";
import {
  sessionFullName,
  sessionInitials,
  sessionRoleLabel,
  type SessionUser,
} from "@/lib/auth/session";
import { cn } from "@/lib/utils";

/** `/admin` only matches itself; the rest also own their subroutes. */
function isActive(pathname: string, href: string): boolean {
  return href === "/admin" ? pathname === href : pathname.startsWith(`${href}/`) || pathname === href;
}

/**
 * Sidebar of the admin shell, measured from the `Sidebar` frame: 240 wide,
 * 20px vertical / 16px horizontal padding, 24px gaps, white with a right
 * hairline.
 *
 * It is a client component because the active entry depends on the current
 * path and a layout never receives it. The footer sits directly after the nav
 * block exactly where the frame draws it, not anchored to the viewport.
 */
export function AdminSidebar({
  counts,
  user,
}: {
  counts: AdminNavigationCounts;
  user: SessionUser | null;
}) {
  const pathname = usePathname();

  return (
    <aside className="hidden w-[240px] shrink-0 flex-col gap-6 border-r border-wf-line bg-wf-surface px-4 py-5 lg:flex">
      <div className="flex items-center gap-3 px-2">
        <span aria-hidden className="block size-8 rounded-wf-sm bg-wf-muted" />
        <span className="flex flex-col gap-1">
          <span className="text-base font-bold tracking-tight text-wf-ink">
            LA TIENDITA
          </span>
          <span className="text-xs text-wf-ink-muted">Panel de administración</span>
        </span>
      </div>

      <nav aria-label="Navegación de administración" className="flex flex-col gap-6">
        {adminNavSections.map((section) => (
          <div key={section.title} className="flex flex-col gap-1">
            <span className="text-xs text-wf-ink-muted">{section.title}</span>
            {section.items.map((item) => (
              <SidebarLink
                key={item.href}
                item={item}
                active={isActive(pathname, item.href)}
                count={item.badge ? counts[item.badge] : undefined}
              />
            ))}
          </div>
        ))}
      </nav>

      <div className="flex flex-col gap-3 border-t border-wf-line pt-4">
        <Link
          href="/"
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
        >
          Ver tienda ↗
        </Link>

        <div
          className="flex items-center gap-3 px-2"
          title={user ? `${user.email} · ${sessionRoleLabel(user)}` : "Sin sesión"}
        >
          <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-wf-subtle text-xs font-bold text-wf-ink-secondary">
            {user ? sessionInitials(user) : "--"}
          </span>
          <span className="flex min-w-0 flex-col gap-1">
            <span className="truncate text-sm font-medium text-wf-ink">
              {user ? sessionFullName(user) : "Sin sesión"}
            </span>
            <span className="text-xs text-wf-ink-muted">
              {user ? sessionRoleLabel(user) : "Invitado"}
            </span>
          </span>
        </div>
      </div>
    </aside>
  );
}

type SidebarLinkProps = {
  item: AdminNavItem;
  active: boolean;
  count: number | undefined;
};

function SidebarLink({ item, active, count }: SidebarLinkProps) {
  return (
    <Link
      href={item.href}
      aria-current={active ? "page" : undefined}
      className={cn(
        "flex h-10 items-center gap-3 rounded-wf-sm px-3 text-sm transition-colors",
        active
          ? "bg-wf-inverse font-medium text-wf-on-inverse"
          : "text-wf-ink-secondary hover:bg-wf-canvas",
      )}
    >
      <span
        aria-hidden
        className={cn(
          "block size-5 shrink-0 rounded-wf-sm",
          active ? "bg-wf-line-strong" : "bg-wf-subtle",
        )}
      />
      <span className="flex-1">{item.label}</span>
      {count === undefined ? null : (
        <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-wf-subtle px-2 text-xs font-bold text-wf-ink-secondary">
          {count}
        </span>
      )}
    </Link>
  );
}
