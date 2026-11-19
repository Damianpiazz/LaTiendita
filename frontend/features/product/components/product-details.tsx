import { cn } from "@/lib/utils";
import type { ProductDetail } from "@/features/product/types";

/** Tabs, description and specs block of the detail screen. */
export function ProductDetails({ product }: { product: ProductDetail }) {
  const tabs = [
    "Descripción",
    "Especificaciones",
    "Envíos y devoluciones",
    `Reseñas (${product.reviewCount})`,
  ];

  return (
    <section aria-labelledby="producto-detalle" className="px-12 pt-10 pb-12">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-7">
        <div className="flex flex-col">
          <div className="flex flex-wrap gap-8">
            {tabs.map((tab, index) => (
              <div key={tab} className="flex flex-col gap-2">
                <span
                  className={cn(
                    "text-[15px]",
                    index === 0
                      ? "font-bold text-wf-ink"
                      : "text-wf-ink-muted",
                  )}
                >
                  {tab}
                </span>
                <span
                  aria-hidden
                  className={cn(
                    "h-[3px] rounded-full",
                    index === 0 ? "bg-wf-inverse" : "bg-transparent",
                  )}
                />
              </div>
            ))}
          </div>
          <div aria-hidden className="h-px bg-wf-line" />
        </div>

        <div className="flex flex-col gap-12 lg:flex-row">
          <div className="flex w-full flex-col gap-3.5 lg:w-[736px] lg:shrink-0">
            <h2 id="producto-detalle" className="text-lg font-bold text-wf-ink">
              Descripción
            </h2>

            {product.description.map((paragraph) => (
              <p
                key={paragraph}
                className="text-sm leading-6 text-wf-ink-secondary"
              >
                {paragraph}
              </p>
            ))}

            <ul className="flex flex-col gap-2 text-sm leading-6 text-wf-ink-secondary">
              {product.highlights.map((highlight) => (
                <li key={highlight}>· {highlight}</li>
              ))}
            </ul>
          </div>

          <div className="w-full self-start overflow-hidden rounded-wf-card border border-wf-line bg-wf-surface lg:w-[560px] lg:shrink-0">
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
        </div>
      </div>
    </section>
  );
}
