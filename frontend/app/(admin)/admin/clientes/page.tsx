import type { Metadata } from "next";

import { AdminClientsPage } from "@/features/admin";

export const metadata: Metadata = {
  title: "Clientes",
};

export default function Page() {
  return <AdminClientsPage />;
}
