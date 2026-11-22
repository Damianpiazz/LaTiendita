"use client";

import { useActionState } from "react";

import { CartEmpty } from "@/features/cart/components/cart-empty";
import { buildCartLines, countUnits, sumSubtotal } from "@/features/cart/helpers/cart-totals";
import { useCartStore } from "@/features/cart/store/cart-store";
import { OrderSummary } from "@/features/checkout";
import type { Product } from "@/features/product/types";
import {
  saveShippingAddress,
  type SaveShippingAddressState,
} from "@/features/shipment/actions/shipping.actions";
import type { ShippingAddress } from "@/features/shipment/types";

import { AddressForm } from "./address-form";
import { ShippingAddressCard } from "./shipping-address-card";

type ShippingViewProps = {
  products: Product[];
  addresses: ShippingAddress[];
  methodName: string;
};

const INITIAL_STATE: SaveShippingAddressState = { ok: false };
const FORM_ID = "shipping-address-form";

function pickInitialAddress(addresses: ShippingAddress[]): ShippingAddress | undefined {
  return addresses.find((address) => address.isDefault) ?? addresses[0];
}

/**
 * Address block and summary of the shipping step.
 *
 * The cart totals come from the client store. The address is loaded from the
 * real shipment-service and saved through a server action before continuing to
 * payment.
 */
export function ShippingView({ products, addresses, methodName }: ShippingViewProps) {
  const items = useCartStore((state) => state.items);
  const lines = buildCartLines(items, products);
  const units = countUnits(lines);
  const subtotal = sumSubtotal(lines);
  const [state, formAction, isPending] = useActionState(saveShippingAddress, INITIAL_STATE);
  const address = pickInitialAddress(addresses);

  if (lines.length === 0) {
    return <CartEmpty />;
  }

  return (
    <div className="flex flex-col gap-6 lg:flex-row">
      <div className="flex min-w-0 flex-1 flex-col gap-6">
        <ShippingAddressCard methodName={methodName}>
          <AddressForm formId={FORM_ID} action={formAction} state={state} address={address} />
        </ShippingAddressCard>
      </div>

      <OrderSummary
        units={units}
        subtotal={subtotal}
        total={subtotal}
        primaryAction={{
          label: isPending ? "Guardando..." : "Continuar al pago",
          type: "submit",
          form: FORM_ID,
          disabled: isPending,
        }}
        secondaryAction={{ label: "Volver al carrito", href: "/carrito" }}
      />
    </div>
  );
}