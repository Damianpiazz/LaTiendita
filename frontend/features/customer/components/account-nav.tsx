"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";

import { logout } from "@/features/auth/services/auth.service";
import { SESSION_EVENT } from "@/lib/auth/session";
import { cn } from "@/lib/utils";

const accountNavItems = [
  { label: "Resumen", href: "/mi-cuenta" },
  { label: "Mis pedidos", href: "/mi-cuenta/pedidos" },
  { label: "Actividad", href: "/mi-cuenta/actividad" },
  { label: "Direcciones", href: "/mi-cuenta/direcciones" },
  { label: "Datos personales", href: "/mi-cuenta/datos" },
  { label: "Newsletter", href: "/mi-cuenta/newsletter" },
];

/** `/mi-cuenta` only matches itself; the rest also own their subroutes. */
function isActive(pathname: string, href: string): boolean {
  return href === "/mi-cuenta"
    ? pathname === href
    : pathname.startsWith(`${href}/`) || pathname === href;
}

/**
 * Account nav of the `/mi-cuenta` screen, measured from the `Account nav`
 * frame: 240px white card, 12px radius, hairline, 16px padding, 4px gaps.
 *
 * Icons are 20px placeholder squares (`subtle`, `line/strong` when active), the
 * same convention the admin sidebar uses. Only `Resumen` exists today; the
 * other entries are the planned screens, linked like the admin ones.
 */
export function AccountNav() {
  const pathname = usePathname();
  const router = useRouter();

  async function handleLogout() {
    await logout();
    window.dispatchEvent(new Event(SESSION_EVENT));
    router.push("/");
    router.refresh();
  }

  return (
    <aside className="w-full shrink-0 lg:w-[240px]">
      <nav
        aria-label="Mi cuenta"
        className="flex flex-col rounded-wf-md border border-wf-line bg-wf-surface p-4"
      >
        <span className="text-xs text-wf-ink-muted">MI CUENTA</span>

        <div className="mt-4 flex flex-col gap-1">
          {accountNavItems.map((item) => {
            const active = isActive(pathname, item.href);

            return (
              <Link
                key={item.href}
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
                {item.label}
              </Link>
            );
          })}
        </div>

        <span aria-hidden className="my-4 h-px bg-wf-line" />

        <button
          type="button"
          onClick={handleLogout}
          className="flex h-10 w-full items-center gap-3 rounded-wf-sm px-3 text-left text-sm text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
        >
          <span aria-hidden className="block size-5 shrink-0 rounded-wf-sm bg-wf-subtle" />
          Cerrar sesión
        </button>
      </nav>
    </aside>
  );
}
