import Link from "next/link";

import { BrandLogo } from "@/components/layout/brand-logo";
import { siteConfig } from "@/config/site";

/**
 * Public footer measured from the `Footer` frame (0:497): white surface with a
 * top hairline, 48px padding, 32px gap, brand column 300 wide, three link
 * columns on a 64px gap and a centered legal bar.
 */
export function SiteFooter() {
  const { footer } = siteConfig;

  return (
    <footer id="contacto" className="border-t border-wf-line bg-wf-surface">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-8 px-12 py-12">
        <div className="flex flex-col gap-8 lg:flex-row lg:justify-between">
          <div className="flex w-full flex-col gap-3.5 lg:w-[300px] lg:shrink-0">
            <BrandLogo />
            <p className="text-[13px] leading-4 text-wf-ink-muted">{footer.tagline}</p>
            <ul className="flex gap-2.5">
              {footer.socials.map((social) => (
                <li key={social.label}>
                  <Link
                    href={social.href}
                    className="block size-7 rounded-wf-mark border border-wf-line-strong bg-wf-surface transition-colors hover:bg-wf-canvas"
                  >
                    <span className="sr-only">{social.label}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div className="flex flex-wrap gap-x-16 gap-y-8">
            {footer.columns.map((column) => (
              <nav key={column.title} aria-label={column.title} className="flex flex-col gap-3">
                <h2 className="text-[13px] font-bold text-wf-ink">{column.title}</h2>
                <ul className="flex flex-col gap-3">
                  {column.links.map((link) => (
                    <li key={link.label}>
                      <Link
                        href={link.href}
                        className="text-[13px] text-wf-ink-muted transition-colors hover:text-wf-ink"
                      >
                        {link.label}
                      </Link>
                    </li>
                  ))}
                </ul>
              </nav>
            ))}
          </div>
        </div>

        <div className="border-t border-wf-line py-5 text-center text-xs text-wf-ink-placeholder">
          {footer.legal}
        </div>
      </div>
    </footer>
  );
}
