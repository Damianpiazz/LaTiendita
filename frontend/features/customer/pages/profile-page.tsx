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

/**
 * `Perfil Cliente Wireframe` of the design file (`2101537842:6`).
 *
 * One screen: breadcrumb, account nav, then the profile header, identity card,
 * stat strip, activity, purchases and the address/preferences row, closing with
 * the benefits strip the frame draws above the footer.
 */
export async function ProfilePage() {
  const profile = await getCustomerProfile();

  return (
    <div>
      <AccountBreadcrumb />

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

      <BenefitsBar />
    </div>
  );
}
