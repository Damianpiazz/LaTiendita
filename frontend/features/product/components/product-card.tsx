import Link from "next/link";

import { ImagePlaceholder } from "@/components/ui/image-placeholder";
import { AddToCartButton } from "@/features/cart/components/add-to-cart-button";
import { formatPrice } from "@/features/product/helpers/format-price";
import type { Product } from "@/features/product/types";
import { cn } from "@/lib/utils";

type ProductCardVariant = "grid" | "compact" | "catalog";

type ProductCardProps = {
  product: Product;
  /**
   * `grid` is the home card (4/3 image, outlined action); `compact` is the
   * detail "Related" card measured at 318×350 (200px image, solid action,
   * 15/17/13 type); `catalog` is the category card measured at 318×352 (200px
   * image, solid action, 16/18/14 type).
   */
  variant?: ProductCardVariant;
};

/** Classes each variant measured from its frame. */
const cardStyles = {
  grid: {
    frame: "rounded-wf-md",
    image: "aspect-[4/3]",
    body: "gap-1 p-4",
    label: "uppercase tracking-wide",
    name: "text-sm font-medium",
    price: "mt-2 text-lg font-semibold",
    action: "mt-4",
  },
  compact: {
    frame: "rounded-wf-card",
    image: "h-[200px] rounded-none text-xs",
    body: "gap-2 p-4",
    label: "",
    name: "text-[15px] font-bold",
    price: "text-[17px] font-bold",
    action:
      "mt-2 h-10 border-transparent bg-wf-inverse text-[13px] font-medium text-wf-on-inverse hover:bg-wf-ink hover:text-wf-on-inverse",
  },
  catalog: {
    frame: "rounded-wf-md",
    image: "h-[200px] rounded-none text-xs",
    body: "gap-2 p-4",
    label: "",
    name: "text-base font-bold",
    price: "text-[18px] font-bold",
    action:
      "mt-2 h-10 border-transparent bg-wf-inverse text-sm font-medium text-wf-on-inverse hover:bg-wf-ink hover:text-wf-on-inverse",
  },
} satisfies Record<ProductCardVariant, Record<string, string>>;

/**
 * Product card: image, category or subcategory, name, price and the
 * add-to-cart action.
 */
export function ProductCard({ product, variant = "grid" }: ProductCardProps) {
  const productHref = `/productos/${product.slug}`;
  const styles = cardStyles[variant];
  const label = variant === "catalog" ? product.subcategory : product.categoryName;

  return (
    <article
      className={cn(
        "flex w-full flex-col overflow-hidden border border-wf-line bg-wf-surface",
        styles.frame,
      )}
    >
      <Link href={productHref} aria-label={product.name}>
        <ImagePlaceholder className={cn("w-full", styles.image)} />
      </Link>

      <div className={cn("flex flex-1 flex-col", styles.body)}>
        <span className={cn("text-xs text-wf-ink-muted", styles.label)}>
          {label}
        </span>
        <Link
          href={productHref}
          className={cn(
            "text-wf-ink transition-colors hover:text-wf-ink-secondary",
            styles.name,
          )}
        >
          {product.name}
        </Link>
        <span className={cn("text-wf-ink", styles.price)}>
          {formatPrice(product.price)}
        </span>
        <AddToCartButton productId={product.id} className={styles.action} />
      </div>
    </article>
  );
}
