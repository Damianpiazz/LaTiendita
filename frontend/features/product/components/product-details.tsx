"use client";

import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import { ProductDescription } from "@/features/product/components/product-description";
import { ProductSpecifications } from "@/features/product/components/product-specifications";
import type { ProductDetail } from "@/features/product/types";

/**
 * Active tab styling shared by both triggers.
 *
 * The shadcn primitive stretches triggers with `flex-1` and paints a
 * `bg-background` block on the active one. Both fight the wireframe, which
 * shows compact labels with a 3px ink underline over a single hairline rule.
 */
const triggerClassName =
  "h-auto flex-none rounded-none border-0 bg-transparent px-0.5 py-3 text-[15px] font-normal text-wf-ink-muted shadow-none transition-all hover:text-wf-ink-secondary active:scale-[0.98] data-active:bg-transparent data-active:font-bold data-active:text-wf-ink data-active:shadow-none after:-bottom-px after:h-[3px] after:bg-wf-inverse focus-visible:ring-2 focus-visible:ring-wf-inverse/40 focus-visible:ring-offset-2";

/**
 * Interactive description/specifications tabs of the detail screen.
 *
 * This is the only stateful UI boundary on the page: the active tab lives
 * here while `ProductDetailPage` stays a server data orchestrator. Reviews
 * render outside this tree and remain visible regardless of the active tab.
 */
export function ProductDetails({ product }: { product: ProductDetail }) {
  return (
    <section aria-label="Detalle del producto" className="px-12 pt-10 pb-12">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col">
        <Tabs defaultValue="descripcion" className="gap-7">
          <TabsList
            variant="line"
            className="h-auto w-full justify-start gap-8 rounded-none border-b border-wf-line bg-transparent p-0 group-data-horizontal/tabs:h-auto"
          >
            <TabsTrigger value="descripcion" className={triggerClassName}>
              Descripción
            </TabsTrigger>
            <TabsTrigger value="especificaciones" className={triggerClassName}>
              Especificaciones
            </TabsTrigger>
          </TabsList>

          <TabsContent value="descripcion">
            <ProductDescription product={product} />
          </TabsContent>
          <TabsContent value="especificaciones">
            <ProductSpecifications product={product} />
          </TabsContent>
        </Tabs>
      </div>
    </section>
  );
}
