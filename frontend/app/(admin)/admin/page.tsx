import type { Metadata } from "next";
import { Suspense } from "react";

import { AdminSummaryPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Resumen",
};

export default function Page() {
  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <AdminSummaryPage />
    </Suspense>
  );
}
