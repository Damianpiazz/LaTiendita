import Link from "next/link";

import { ImagePlaceholder } from "@/components/ui/image-placeholder";
import type { Category } from "@/features/product/types";

/** Category tile of the home grid. */
export function CategoryCard({ category }: { category: Category }) {
  return (
    <Link
      href={`/categorias/${category.slug}`}
      className="group flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-4 transition-colors hover:border-wf-line-strong"
    >
      <ImagePlaceholder className="aspect-[4/3] w-full" />
      <span className="text-sm font-medium text-wf-ink">{category.name}</span>
    </Link>
  );
}
