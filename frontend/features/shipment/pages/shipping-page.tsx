import { BenefitsBar } from "@/components/layout/benefits-bar";
import { CheckoutSteps } from "@/features/checkout";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { getProducts } from "@/features/product/services/catalog.service";
import { ShippingView } from "@/features/shipment/components/shipping-view";
import { getShippingMethods } from "@/features/shipment/services/shipping.service";

/**
 * `Checkout Shipping Wireframe` of the design file.
 *
 * The catalog and the shipping methods arrive through the cached services, so
 * the screen follows the same mock (or real) API as the rest of the store; the
 * cart totals are the client store.
 */
export async function ShippingPage() {
  const [products, methods] = await Promise.all([
    getProducts(),
    getShippingMethods(),
  ]);

  return (
    <>
      <Breadcrumbs
        size="md"
        items={[
          { label: "Inicio", href: "/" },
          { label: "Carrito", href: "/carrito" },
          { label: "Envío" },
        ]}
      />

      <section className="px-12 py-10">
        <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-8">
          <CheckoutSteps currentStep={2} />

          <h1 className="text-2xl font-bold text-wf-ink">Envío</h1>

          <ShippingView products={products} methods={methods} />
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
