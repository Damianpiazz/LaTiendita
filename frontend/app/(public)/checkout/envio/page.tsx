import type { Metadata } from "next";

import { ShippingPage } from "@/features/shipment/pages/shipping-page";

export const metadata: Metadata = {
  title: "Envío",
};

export default function Page() {
  return <ShippingPage />;
}
