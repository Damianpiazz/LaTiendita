import { buildPageTokens } from "@/features/admin/helpers/pagination";
import { cn } from "@/lib/utils";

type AdminPaginationProps = {
  label: string;
  pageCount: number;
  current?: number;
  /** Absent on the still-presentational admin tables, which keep static tokens. */
  onPageChange?: (page: number) => void;
};

/** Footer pagination shared by the admin tables. */
export function AdminPagination({
  label,
  pageCount,
  current = 1,
  onPageChange,
}: AdminPaginationProps) {
  const tokens = buildPageTokens(pageCount);

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <p className="text-sm text-wf-ink-muted">{label}</p>

      <div className="flex items-center gap-2">
        {onPageChange ? (
          <button
            type="button"
            aria-label="Página anterior"
            disabled={current <= 1}
            onClick={() => onPageChange(current - 1)}
            className="flex size-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:border-wf-line disabled:text-wf-ink-placeholder"
          >
            <span aria-hidden>←</span>
          </button>
        ) : (
          <span
            aria-hidden
            className="flex size-10 items-center justify-center text-wf-ink-placeholder"
          >
            ←
          </span>
        )}

        {tokens.map((token) =>
          token === "ellipsis" ? (
            <span
              key="ellipsis"
              aria-hidden
              className="px-1 text-wf-ink-placeholder"
            >
              …
            </span>
          ) : onPageChange ? (
            <button
              key={token}
              type="button"
              aria-current={token === current ? "page" : undefined}
              onClick={() => onPageChange(token)}
              className={cn(
                "flex size-10 items-center justify-center rounded-wf-sm border text-sm transition-colors",
                token === current
                  ? "border-wf-inverse bg-wf-inverse font-medium text-wf-on-inverse"
                  : "border-wf-line-strong bg-wf-surface text-wf-ink-secondary hover:bg-wf-canvas",
              )}
            >
              {token}
            </button>
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

        {onPageChange ? (
          <button
            type="button"
            aria-label="Página siguiente"
            disabled={current >= pageCount}
            onClick={() => onPageChange(current + 1)}
            className="flex size-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:border-wf-line disabled:text-wf-ink-placeholder"
          >
            <span aria-hidden>→</span>
          </button>
        ) : (
          <span className="flex size-10 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-wf-ink-secondary">
            →
          </span>
        )}
      </div>
    </div>
  );
}
