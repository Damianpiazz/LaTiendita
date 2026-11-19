import { BenefitsBar } from "@/components/layout/benefits-bar";
import { CartView } from "@/features/cart/components/cart-view";
import { CheckoutSteps } from "@/features/checkout";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { getProducts } from "@/features/product/services/catalog.service";

/**
 * `Cart Wireframe` of the design file.
 *
 * The catalog arrives through the cached service, so prices and names follow
 * the same mock (or real) API as the rest of the store; the cart itself is the
 * client store.
 */
export async function CartPage() {
  const products = await getProducts();

  return (
    <>
      <Breadcrumbs
        size="md"
        items={[{ label: "Inicio", href: "/" }, { label: "Carrito" }]}
      />

      <section className="px-12 py-10">
        <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-8">
          <CheckoutSteps currentStep={1} />
          <CartView products={products} />
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
