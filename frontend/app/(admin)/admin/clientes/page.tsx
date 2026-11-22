import type { Metadata } from "next";
import { Suspense } from "react";

import { AdminClientsPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Clientes",
};

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <AdminClientsPage searchParams={searchParams} />
    </Suspense>
  );
}
