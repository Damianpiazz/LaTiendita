import { Fragment } from "react";
import Link from "next/link";

import { cn } from "@/lib/utils";

export type BreadcrumbItem = {
  label: string;
  href?: string;
};

type BreadcrumbsProps = {
  items: BreadcrumbItem[];
  /**
   * `sm` is the product screen band (13px text, 44px tall); `md` is the cart
   * band (14px text, 49px tall). Both are measured from their frames.
   */
  size?: "sm" | "md";
};

/** Breadcrumb band: bottom hairline, 48px gutter, centered on a 1440 canvas. */
export function Breadcrumbs({ items, size = "sm" }: BreadcrumbsProps) {
  return (
    <nav aria-label="Migas de pan" className="border-b border-wf-line bg-wf-surface">
      <ol
        className={cn(
          "mx-auto flex w-full max-w-[1440px] flex-wrap items-center gap-2 px-12",
          size === "sm" ? "py-3.5 text-[13px]" : "py-4 text-[14px] leading-[17px]",
        )}
      >
        {items.map((item, index) => {
          const isCurrent = index === items.length - 1;

          return (
            <Fragment key={item.label}>
              {index > 0 ? (
                <li aria-hidden className="text-wf-ink-placeholder">
                  /
                </li>
              ) : null}
              <li>
                {item.href && !isCurrent ? (
                  <Link
                    href={item.href}
                    className="text-wf-ink-muted transition-colors hover:text-wf-ink"
                  >
                    {item.label}
                  </Link>
                ) : (
                  <span
                    className={cn(
                      isCurrent
                        ? "font-medium text-wf-ink"
                        : "text-wf-ink-muted",
                    )}
                    aria-current={isCurrent ? "page" : undefined}
                  >
                    {item.label}
                  </span>
                )}
              </li>
            </Fragment>
          );
        })}
      </ol>
    </nav>
  );
}
