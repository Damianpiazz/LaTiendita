import { ProductStatusBadge } from "@/features/admin/components/product-status-badge";
import type { AdminProducts } from "@/features/admin/types";
import { formatPrice } from "@/features/product/helpers/format-price";
import { cn } from "@/lib/utils";

/**
 * Products table: 40px head plus eight 64px rows.
 *
 * Column widths are the frame's; the wrapper scrolls sideways when the viewport
 * is narrower than the 1096px table. Selection comes from the seed, and stock
 * under the threshold becomes the low-stock pill.
 */
export function AdminProductsTable({
  products,
}: {
  products: AdminProducts;
}) {
  const lowStockThreshold = products.lowStockThreshold;

  return (
    <section className="overflow-hidden rounded-wf-md border border-wf-line bg-wf-surface">
      <div className="overflow-x-auto">
        <div role="table" aria-label="Productos" className="min-w-[1096px]">
          <div
            role="row"
            className="flex h-10 items-center border-b border-wf-line bg-wf-canvas px-5 text-xs text-wf-ink-muted"
          >
            <div role="columnheader" className="w-10" />
            <div role="columnheader" className="w-[440px]">
              Producto
            </div>
            <div role="columnheader" className="w-[150px]">
              Categoría
            </div>
            <div role="columnheader" className="w-[120px]">
              Precio
            </div>
            <div role="columnheader" className="w-[110px]">
              Stock
            </div>
            <div role="columnheader" className="w-[130px]">
              Estado
            </div>
            <div role="columnheader" className="flex-1 text-right">
              Acciones
            </div>
          </div>

          {products.items.map((product) => {
            const lowStock = product.stock < lowStockThreshold;

            return (
              <div
                key={product.id}
                role="row"
                className="flex h-16 items-center border-b border-wf-line px-5 text-sm last:border-b-0"
              >
                <div role="cell" className="w-10">
                  <span
                    aria-hidden
                    className={cn(
                      "flex size-6 items-center justify-center rounded-wf-sm border text-xs font-bold",
                      product.selected
                        ? "border-wf-inverse bg-wf-inverse text-wf-on-inverse"
                        : "border-wf-line-strong bg-wf-surface",
                    )}
                  >
                    {product.selected ? "✓" : ""}
                  </span>
                </div>

                <div role="cell" className="flex w-[440px] min-w-0 flex-col gap-1">
                  <span className="truncate font-medium text-wf-ink">
                    {product.name}
                  </span>
                  <span className="text-xs text-wf-ink-muted">
                    {product.sku}
                  </span>
                </div>

                <div role="cell" className="w-[150px] text-wf-ink-secondary">
                  {product.category}
                </div>
                <div role="cell" className="w-[120px] font-medium text-wf-ink">
                  {formatPrice(product.price)}
                </div>
                <div role="cell" className="w-[110px]">
                  {lowStock ? (
                    <span className="inline-flex h-6 items-center rounded-full bg-wf-subtle px-2 text-xs font-bold text-wf-ink-secondary">
                      {product.stock} u.
                    </span>
                  ) : (
                    <span className="text-wf-ink-secondary">
                      {product.stock} u.
                    </span>
                  )}
                </div>
                <div role="cell" className="w-[130px]">
                  <ProductStatusBadge status={product.status} />
                </div>

                <div
                  role="cell"
                  className="flex flex-1 items-center justify-end gap-3"
                >
                  <span className="font-medium text-wf-ink">Editar</span>
                  <span
                    aria-hidden
                    className="font-bold text-wf-ink-placeholder"
                  >
                    ···
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
