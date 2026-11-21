import type { Metadata } from "next";
import { Suspense } from "react";

import { ShippingPage } from "@/features/shipment/pages/shipping-page";

export const metadata: Metadata = {
  title: "Envío",
};

export default function Page() {
  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <ShippingPage />
    </Suspense>
  );
}
