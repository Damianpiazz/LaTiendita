import Link from "next/link";

import { BrandLogo } from "@/components/layout/brand-logo";
import { siteConfig } from "@/config/site";
import { CartBadge } from "@/features/cart/components/cart-badge";

/**
 * Public header, measured from the `Header` frame of the wireframe:
 * three groups (logo / nav / actions) on `justify-between`, 48px gutter,
 * 16px vertical padding around 40px controls => 72px, bottom hairline.
 *
 * The search field is presentational for now; it will submit to `/productos`
 * once the catalog route exists. `Ingresar` links to the login screen, which
 * already exists even though there is no session behind it yet.
 *
 * `account` replaces that login control with the signed-in chip the profile
 * frame draws (24px avatar with the initials plus the label). It is optional so
 * every other public page keeps the exact header it has today.
 */
type SiteHeaderProps = {
  account?: {
    /** Initials shown inside the 24px avatar, e.g. `LF`. */
    initials: string;
    label: string;
  };
};

export function SiteHeader({ account }: SiteHeaderProps = {}) {
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
          <div
            role="search"
            className="hidden h-10 w-[260px] items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3.5 md:flex"
          >
            <label htmlFor="site-search" className="sr-only">
              Buscar productos
            </label>
            <input
              id="site-search"
              type="search"
              placeholder={siteConfig.searchPlaceholder}
              className="h-full w-full bg-transparent text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
            />
          </div>

          <CartBadge />

          {account ? (
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
