import Link from "next/link";

/**
 * Breadcrumb band of the account screen, measured from the `Breadcrumb` frame:
 * white 49px band, bottom hairline, 16px vertical / 48px horizontal padding.
 * The 17px line height keeps the band at the drawn 49px instead of the 20px a
 * plain `text-sm` would give.
 */
export function AccountBreadcrumb() {
  return (
    <nav aria-label="Migas de pan" className="border-b border-wf-line bg-wf-surface">
      <ol className="mx-auto flex w-full max-w-[1440px] items-center gap-2 px-6 py-4 text-sm leading-[17px] lg:px-12">
        <li>
          <Link
            href="/"
            className="text-wf-ink-muted transition-colors hover:text-wf-ink"
          >
            Inicio
          </Link>
        </li>
        <li aria-hidden className="text-wf-ink-placeholder">
          /
        </li>
        <li aria-current="page" className="font-medium text-wf-ink">
          Mi cuenta
        </li>
      </ol>
    </nav>
  );
}
