import { BenefitsBar } from "@/components/layout/benefits-bar";
import { CheckoutSteps } from "@/features/checkout";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { getProducts } from "@/features/product/services/catalog.service";
import { ShippingView } from "@/features/shipment/components/shipping-view";
import {
  getShippingAddresses,
  getShippingMethods,
} from "@/features/shipment/services/shipping.service";

/**
 * `Checkout Shipping Wireframe` of the design file.
 *
 * The catalog comes from the real catalog service; delivery addresses come from
 * the real shipment-service when the user is authenticated.
 */
export async function ShippingPage() {
  const [products, methods, addresses] = await Promise.all([
    getProducts(),
    getShippingMethods(),
    getShippingAddresses(),
  ]);
  const [method] = methods;

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

          <ShippingView
            products={products}
            addresses={addresses}
            methodName={method?.name ?? "Envío a domicilio"}
          />
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
