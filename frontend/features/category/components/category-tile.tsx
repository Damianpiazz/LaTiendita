import Link from "next/link";

import { ImagePlaceholder } from "@/components/ui/image-placeholder";
import type { Category } from "@/features/product/types";

/**
 * Category tile of the categories screen.
 *
 * Wireframe: 318×232 card, radius 12, white with a `border/default` hairline;
 * 318×160 image, then a 72px body with 16px padding that puts the name and the
 * product count on the left and the arrow on the right.
 */
export function CategoryTile({ category }: { category: Category }) {
  const productLabel = category.productCount === 1 ? "producto" : "productos";

  return (
    <Link
      href={`/categorias/${category.slug}`}
      className="group flex flex-col overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface transition-colors hover:border-wf-line-strong"
    >
      <ImagePlaceholder className="h-40 w-full rounded-none text-xs text-wf-ink-subtle" />

      <span className="flex h-[72px] items-center justify-between gap-4 px-4">
        <span className="flex flex-col gap-1">
          <span className="text-base font-medium text-wf-ink">{category.name}</span>
          <span className="text-sm text-wf-ink-muted">
            {category.productCount} {productLabel}
          </span>
        </span>
        <span
          aria-hidden
          className="text-base text-wf-ink-placeholder transition-colors group-hover:text-wf-ink-secondary"
        >
          →
        </span>
      </span>
    </Link>
  );
}
