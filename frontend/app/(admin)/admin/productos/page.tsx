import type { Metadata } from "next";
import { Suspense } from "react";

import { AdminProductsPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Productos",
};

export default function Page() {
  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <AdminProductsPage />
    </Suspense>
  );
}
