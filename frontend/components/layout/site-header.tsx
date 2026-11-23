"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

import { BrandLogo } from "@/components/layout/brand-logo";
import { siteConfig } from "@/config/site";
import { CartBadge } from "@/features/cart/components/cart-badge";
import { logout } from "@/features/auth/services/auth.service";
import { SESSION_EVENT } from "@/lib/auth/session";

/**
 * Public header, measured from the `Header` frame of the wireframe:
 * three groups (logo / nav / actions) on `justify-between`, 48px gutter,
 * 16px vertical padding around 40px controls => 72px, bottom hairline.
 *
 * `account` replaces the `Ingresar` control with the signed-in chip the profile
 * frame draws (24px avatar with the initials plus the label) and adds `Salir`.
 */
type SiteHeaderProps = {
  account?: {
    /** Initials shown inside the 24px avatar, e.g. `LF`. */
    initials: string;
    label: string;
  };
};

export function SiteHeader({ account }: SiteHeaderProps = {}) {
  const router = useRouter();
  const [search, setSearch] = useState("");

  function handleSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const query = search.trim();

    if (!query) return;

    router.push(`/productos?q=${encodeURIComponent(query)}`);
  }

  async function handleLogout() {
    await logout();
    window.dispatchEvent(new Event(SESSION_EVENT));
    router.push("/");
    router.refresh();
  }

  return (
    <header className="sticky top-0 z-40 border-b border-wf-line bg-wf-surface">
      <div className="mx-auto flex w-full max-w-[1440px] items-center justify-between gap-6 px-12 py-4">
        <Link href="/" aria-label={`${siteConfig.name} — Inicio`} className="shrink-0">
          <BrandLogo />
        </Link>

        <nav
          aria-label="Navegación principal"
          className="hidden items-center gap-7 text-sm text-wf-ink-secondary lg:flex"
        >
          {siteConfig.nav.map((item) => (
            <Link
              key={item.label}
              href={item.href}
              className="transition-colors hover:text-wf-ink"
            >
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          <form
            role="search"
            onSubmit={handleSearch}
            className="hidden h-10 w-[260px] items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3.5 md:flex"
          >
            <label htmlFor="site-search" className="sr-only">
              Buscar productos
            </label>
            <input
              id="site-search"
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder={siteConfig.searchPlaceholder}
              className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
            />
          </form>

          <CartBadge />

          {account ? (
            <>
              <Link
                href="/mi-cuenta"
                className="inline-flex h-10 shrink-0 items-center gap-2 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
              >
                <span
                  aria-hidden
                  className="inline-flex size-6 items-center justify-center rounded-full bg-wf-subtle text-xs font-bold text-wf-ink-secondary"
                >
                  {account.initials}
                </span>
                {account.label}
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                className="h-10 shrink-0 rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
              >
                Salir
              </button>
            </>
          ) : (
            <Link
              href="/ingresar"
              className="flex h-10 shrink-0 items-center rounded-wf-sm bg-wf-inverse px-[18px] text-sm font-medium text-wf-on-inverse transition-colors hover:bg-wf-ink"
            >
              Ingresar
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
