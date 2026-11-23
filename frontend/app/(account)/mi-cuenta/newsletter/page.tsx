import type { Metadata } from "next";

import { AccountPageShell } from "@/features/customer/components/account-page-shell";
import { NewsletterSwitch } from "@/features/customer/components/newsletter-switch";
import { getCustomerProfile } from "@/features/customer/services/customer.service";

export const metadata: Metadata = {
  title: "Newsletter",
};

export default async function NewsletterPage() {
  const profile = await getCustomerProfile();

  return (
    <AccountPageShell
      title="Newsletter"
      description="Elegí si querés recibir novedades y ofertas por email."
    >
      <div className="flex max-w-2xl flex-col gap-5 rounded-wf-md border border-wf-line bg-wf-surface p-6">
        <div className="flex items-center justify-between gap-4">
          <div className="flex flex-col gap-1">
            <span className="text-sm font-medium text-wf-ink">Novedades y ofertas</span>
            <span className="text-sm text-wf-ink-muted">
              {profile.preferences.newsletter ? "Suscripción activa" : "Suscripción desactivada"}
            </span>
          </div>
          <NewsletterSwitch initialValue={profile.preferences.newsletter} />
        </div>

        <p className="text-xs text-wf-ink-muted">{profile.preferences.consentNote}</p>
      </div>
    </AccountPageShell>
  );
}
