import type { Metadata } from "next";

import { AccountPageShell } from "@/features/customer/components/account-page-shell";
import { ProfilePurchases } from "@/features/customer/components/profile-purchases";
import { getCustomerProfile } from "@/features/customer/services/customer.service";

export const metadata: Metadata = {
  title: "Mis pedidos",
};

export default async function OrdersPage() {
  const profile = await getCustomerProfile();

  return (
    <AccountPageShell
      title="Mis pedidos"
      description="Consultá el estado y el detalle de cada una de tus compras."
    >
      <ProfilePurchases orders={profile.orders} />
    </AccountPageShell>
  );
}
