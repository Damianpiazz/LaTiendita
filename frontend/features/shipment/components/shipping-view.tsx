"use client";

import { CartEmpty } from "@/features/cart/components/cart-empty";
import {
  buildCartLines,
  countUnits,
  sumSubtotal,
} from "@/features/cart/helpers/cart-totals";
import { useCartStore } from "@/features/cart/store/cart-store";
import { OrderSummary } from "@/features/checkout";
import type { Product } from "@/features/product/types";
import type { ShippingMethod } from "@/features/shipment/types";

import { AddressForm } from "./address-form";
import { ShippingAddressCard } from "./shipping-address-card";

type ShippingViewProps = {
  products: Product[];
  methods: ShippingMethod[];
};

/**
 * Address block and summary of the shipping step.
 *
 * It is a client component because the subtotal and the unit count come from
 * the cart store, the same state the header badge and the cart screen read.
 * Shipping has no amount on screen, so the total is the subtotal.
 */
export function ShippingView({ products, methods }: ShippingViewProps) {
  const items = useCartStore((state) => state.items);
  const lines = buildCartLines(items, products);
  const units = countUnits(lines);
  const subtotal = sumSubtotal(lines);
  const [method] = methods;

  if (lines.length === 0) {
    return <CartEmpty />;
  }

  if (!method) {
    throw new Error("No shipping methods available");
  }

  return (
    <div className="flex flex-col gap-6 lg:flex-row">
      <div className="flex min-w-0 flex-1 flex-col gap-6">
        <ShippingAddressCard methodName={method.name}>
          <AddressForm />
        </ShippingAddressCard>
      </div>

      <OrderSummary
        units={units}
        subtotal={subtotal}
        total={subtotal}
        primaryAction={{ label: "Continuar al pago", href: "/checkout/pago" }}
        secondaryAction={{ label: "Volver al carrito", href: "/carrito" }}
      />
    </div>
  );
}
