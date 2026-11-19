import type { Metadata } from "next";

import { AdminSummaryPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Resumen",
};

export default function Page() {
  return <AdminSummaryPage />;
}
