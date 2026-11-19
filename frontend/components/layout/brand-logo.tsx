import { cn } from "@/lib/utils";

/**
 * Brand lockup from the wireframe: 28x28 placeholder mark plus the 18/700
 * wordmark. The mark is intentionally undefined (no initials, no glyph) and
 * uses `background/muted`, the token reserved for logo placeholder fills.
 */
export function BrandLogo({ className }: { className?: string }) {
  return (
    <span className={cn("inline-flex items-center gap-2.5", className)}>
      <span aria-hidden className="block size-7 rounded-wf-mark bg-wf-muted" />
      <span className="text-lg font-bold tracking-tight text-wf-ink">
        LA TIENDITA
      </span>
    </span>
  );
}
