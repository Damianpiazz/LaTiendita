import type { Metadata } from "next";
import { Suspense } from "react";

import { AdminOrdersPage } from "@/features/admin";
import { parseAdminRangeDays } from "@/features/admin/helpers/admin-range";

export const metadata: Metadata = {
  title: "Pedidos",
};

type SearchParams = Record<string, string | string[] | undefined>;

function firstValue(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<SearchParams>;
}) {
  const params = await searchParams;
  const q = firstValue(params.q)?.trim();
  const days = parseAdminRangeDays(firstValue(params.days));

  return (
    <Suspense fallback={<div className="min-h-[50vh] bg-wf-canvas" aria-busy="true" />}>
      <AdminOrdersPage
        initialQuery={{
          ...(q ? { q } : {}),
          ...(days ? { days } : {}),
        }}
      />
    </Suspense>
  );
}
