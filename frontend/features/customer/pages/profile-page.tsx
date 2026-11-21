import { Suspense } from "react";

import { BenefitsBar } from "@/components/layout/benefits-bar";
import { AccountBreadcrumb } from "@/features/customer/components/account-breadcrumb";
import { AccountNav } from "@/features/customer/components/account-nav";
import { ProfileActivity } from "@/features/customer/components/profile-activity";
import { ProfileCard } from "@/features/customer/components/profile-card";
import { ProfileDetails } from "@/features/customer/components/profile-details";
import { ProfileHeader } from "@/features/customer/components/profile-header";
import { ProfilePurchases } from "@/features/customer/components/profile-purchases";
import { ProfileStats } from "@/features/customer/components/profile-stats";
import { getCustomerProfile } from "@/features/customer/services/customer.service";

async function ProfileContent() {
  const profile = await getCustomerProfile();

  return (
    <section className="bg-wf-canvas">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-6 px-6 pb-16 pt-10 lg:flex-row lg:px-12">
        <AccountNav />

        <div className="flex min-w-0 flex-1 flex-col gap-8">
          <ProfileHeader profile={profile} />
          <ProfileCard profile={profile} />
          <ProfileStats stats={profile.stats} />
          <ProfileActivity activity={profile.activity} />
          <ProfilePurchases orders={profile.orders} />
          <ProfileDetails
            address={profile.address}
            preferences={profile.preferences}
          />
        </div>
      </div>
    </section>
  );
}

/**
 * `Perfil Cliente Wireframe` of the design file (`2101537842:6`).
 *
 * The session-scoped profile streams inside a Suspense boundary so the static
 * shell can still be prerendered.
 */
export function ProfilePage() {
  return (
    <div>
      <AccountBreadcrumb />

      <Suspense fallback={<div className="min-h-[60vh] bg-wf-canvas" aria-busy="true" />}>
        <ProfileContent />
      </Suspense>

      <BenefitsBar />
    </div>
  );
}
