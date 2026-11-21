import type { Metadata } from "next";
import { Suspense } from "react";

import { AdminClientsPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Clientes",
};

export default function Page() {
  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <AdminClientsPage />
    </Suspense>
  );
}
