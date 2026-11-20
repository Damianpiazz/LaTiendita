import type { ProductDetail } from "@/features/product/types";

/** Specifications panel of the product detail tabs. */
export function ProductSpecifications({ product }: { product: ProductDetail }) {
  return (
    <div className="w-full overflow-hidden rounded-wf-card border border-wf-line bg-wf-surface">
      <h3 className="px-4 py-3.5 text-[15px] font-bold text-wf-ink">
        Especificaciones
      </h3>
      <dl>
        {product.specs.map((spec) => (
          <div
            key={spec.label}
            className="flex items-center justify-between border-t border-wf-line px-4 py-3 text-[13px]"
          >
            <dt className="text-wf-ink-muted">{spec.label}</dt>
            <dd className="text-wf-ink">{spec.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
