import { buildPageTokens } from "@/features/admin/helpers/pagination";
import { cn } from "@/lib/utils";

/**
 * Footer pagination shared by the products and orders tables.
 *
 * The current page is inverse; the rest are outlined controls. Nothing is
 * clickable yet because the mock always serves the first page — the frame only
 * draws their resting state.
 */
export function AdminPagination({
  label,
  pageCount,
  current = 1,
}: {
  label: string;
  pageCount: number;
  current?: number;
}) {
  const tokens = buildPageTokens(pageCount);

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <p className="text-sm text-wf-ink-muted">{label}</p>

      <div className="flex items-center gap-2">
        <span
          aria-hidden
          className="flex size-10 items-center justify-center text-wf-ink-placeholder"
        >
          ←
        </span>

        {tokens.map((token) =>
          token === "ellipsis" ? (
            <span
              key="ellipsis"
              aria-hidden
              className="px-1 text-wf-ink-placeholder"
            >
              …
            </span>
          ) : (
            <span
              key={token}
              aria-current={token === current ? "page" : undefined}
              className={cn(
                "flex size-10 items-center justify-center rounded-wf-sm border text-sm",
                token === current
                  ? "border-wf-inverse bg-wf-inverse font-medium text-wf-on-inverse"
                  : "border-wf-line-strong bg-wf-surface text-wf-ink-secondary",
              )}
            >
              {token}
            </span>
          ),
        )}

        <span className="flex size-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-wf-ink-secondary">
          →
        </span>
      </div>
    </div>
  );
}
