import type { Metadata } from "next";

import { AccountPageShell } from "@/features/customer/components/account-page-shell";
import { ProfileActivity } from "@/features/customer/components/profile-activity";
import { getCustomerProfile } from "@/features/customer/services/customer.service";

export const metadata: Metadata = {
  title: "Actividad",
};

export default async function ActivityPage() {
  const profile = await getCustomerProfile({ activityLimit: 50 });

  return (
    <AccountPageShell
      title="Actividad"
      description="Pedidos y reseñas recientes de tu cuenta, de más nuevo a más viejo."
    >
      <ProfileActivity activity={profile.activity} />
    </AccountPageShell>
  );
}
