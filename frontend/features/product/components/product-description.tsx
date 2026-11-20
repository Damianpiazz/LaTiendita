import type { ProductDetail } from "@/features/product/types";

/** Description panel of the product detail tabs. */
export function ProductDescription({ product }: { product: ProductDetail }) {
  return (
    <div className="flex w-full flex-col gap-3.5">
      <h2 className="text-lg font-bold text-wf-ink">
        Descripción
      </h2>

      {product.description.map((paragraph) => (
        <p key={paragraph} className="text-sm leading-6 text-wf-ink-secondary">
          {paragraph}
        </p>
      ))}

      <ul className="flex flex-col gap-2 text-sm leading-6 text-wf-ink-secondary">
        {product.highlights.map((highlight) => (
          <li key={highlight}>· {highlight}</li>
        ))}
      </ul>
    </div>
  );
}
