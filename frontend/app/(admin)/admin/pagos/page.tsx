import type { Metadata } from "next";

import { AdminPaymentsPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Pagos",
};

export default function Page() {
  return <AdminPaymentsPage />;
}
