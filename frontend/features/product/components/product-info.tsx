import { siteConfig } from "@/config/site";
import { PurchasePanel } from "@/features/cart/components/purchase-panel";
import { formatPrice } from "@/features/product/helpers/format-price";
import type { ProductDetail } from "@/features/product/types";

import { RatingStars } from "./rating-stars";

/** Info column of the detail screen: identity, price, purchase actions and perks. */
export function ProductInfo({ product }: { product: ProductDetail }) {
  const hasReviews = product.reviewCount > 0;
  const inStock = product.stock > 0;
  const discount =
    product.originalPrice === null
      ? null
      : Math.round((1 - product.price / product.originalPrice) * 100);
  const installmentAmount = Math.round(product.price / product.installmentCount);

  return (
    <div className="flex min-w-0 flex-col gap-3.5">
      <span className="text-[13px] text-wf-ink-muted">
        {product.categoryName} · SKU {product.sku}
      </span>

      <h1 className="text-[34px] font-bold leading-tight tracking-tight text-wf-ink">
        {product.name}
      </h1>

      {hasReviews ? (
        <div className="flex flex-wrap items-center gap-2 text-[13px] text-wf-ink-muted">
          <RatingStars filled={4} />
          <span>
            {product.rating.toLocaleString("es-AR", {
              minimumFractionDigits: 1,
              maximumFractionDigits: 1,
            })}{" "}
            de 5
          </span>
          <span aria-hidden className="text-wf-ink-placeholder">
            ·
          </span>
          <span>{product.reviewCount} reseñas</span>
          <span aria-hidden className="text-wf-ink-placeholder">
            ·
          </span>
          <span>{product.soldCount}+ vendidos</span>
        </div>
      ) : (
        <span className="text-[13px] text-wf-ink-muted">Sin reseñas todavía</span>
      )}

      <div aria-hidden className="h-px bg-wf-line" />

      <div className="flex flex-wrap items-center gap-3">
        <span className="text-[32px] font-bold leading-none text-wf-ink">
          {formatPrice(product.price)}
        </span>
        {product.originalPrice !== null ? (
          <span className="text-base text-wf-ink-placeholder line-through">
            {formatPrice(product.originalPrice)}
          </span>
        ) : null}
        {discount !== null ? (
          <span className="rounded-wf-xs bg-wf-inverse px-2 py-1 text-xs font-bold text-wf-on-inverse">
            -{discount}% OFF
          </span>
        ) : null}
      </div>

      <span className="text-[13px] text-wf-ink-muted">
        {product.installmentCount}{" "}
        {product.installmentCount === 1 ? "cuota" : "cuotas"} sin interés de{" "}
        {formatPrice(installmentAmount)}
      </span>

      <span className="flex items-center gap-2 text-[13px] text-wf-ink-secondary">
        <span
          aria-hidden
          className={
            inStock ? "size-2 rounded-full bg-wf-inverse" : "size-2 rounded-full bg-wf-muted"
          }
        />
        {inStock ? `En stock — sale en ${product.fulfillment}` : "Sin stock"}
      </span>

      <div aria-hidden className="h-px bg-wf-line" />

      <PurchasePanel productId={product.id} />

      <div aria-hidden className="h-px bg-wf-line" />

      <ul className="flex flex-col gap-3 pt-0.5">
        {siteConfig.productPerks.map((perk) => (
          <li key={perk.title} className="flex items-center gap-3.5">
            <span aria-hidden className="size-9 shrink-0 rounded-wf-sm bg-wf-subtle" />
            <span className="flex flex-col gap-0.5">
              <span className="text-[13px] font-medium text-wf-ink">{perk.title}</span>
              <span className="text-xs text-wf-ink-muted">{perk.note}</span>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
