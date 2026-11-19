import type { Metadata } from "next";

import { AdminProductsPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Productos",
};

export default function Page() {
  return <AdminProductsPage />;
}
