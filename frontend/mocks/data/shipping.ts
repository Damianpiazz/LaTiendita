import type { ShippingMethod } from "@/features/shipment/types";

/**
 * Shipping of the checkout.
 *
 * The store ships to a home address only, so the method is a single entry: the
 * screen states it instead of offering a choice.
 */
export const shippingMethods: ShippingMethod[] = [
  {
    id: "envio-a-domicilio",
    name: "Envío a domicilio",
  },
];
