import type { Metadata } from "next";

import { AccountPageShell } from "@/features/customer/components/account-page-shell";
import { ProfileForm } from "@/features/customer/components/profile-form";
import { getCustomerProfile } from "@/features/customer/services/customer.service";

export const metadata: Metadata = {
  title: "Datos personales",
};

export default async function PersonalDataPage() {
  const profile = await getCustomerProfile();

  return (
    <AccountPageShell
      title="Datos personales"
      description="Actualizá tu nombre, apellido y teléfono de contacto."
    >
      <ProfileForm profile={profile} />
    </AccountPageShell>
  );
}
