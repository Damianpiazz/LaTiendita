import type { ReactNode } from "react";

type ShippingAddressCardProps = {
  /** Name of the store's only shipping method. */
  methodName: string;
  /** Address form the delivery is set up with. */
  children: ReactNode;
};

/**
 * Shipping block of the checkout.
 *
 * The store ships to a home address only, so there is nothing to select: the
 * card states the method and then asks for the address. Wireframe: white card
 * with a `border/default` hairline, radius 12, 20px padding and 20px gaps.
 */
export function ShippingAddressCard({
  methodName,
  children,
}: ShippingAddressCardProps) {
  return (
    <div className="flex flex-col gap-5 rounded-wf-md border border-wf-line bg-wf-surface p-5">

      {children}
    </div>
  );
}
