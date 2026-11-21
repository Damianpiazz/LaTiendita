import type { Metadata } from "next";
import { Suspense } from "react";

import { AdminCategoriesPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Categorías",
};

export default function Page() {
  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <AdminCategoriesPage />
    </Suspense>
  );
}
