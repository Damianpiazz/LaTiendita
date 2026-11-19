import type { Metadata } from "next";

import { AdminOrdersPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Pedidos",
};

export default function Page() {
  return <AdminOrdersPage />;
}
