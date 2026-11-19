import { SiteFooter } from "@/components/layout/site-footer";
import { SiteHeader } from "@/components/layout/site-header";
import { getCustomerProfile } from "@/features/customer/services/customer.service";

/**
 * Customer account shell: the public header and footer, with the header showing
 * the signed-in control instead of the `Ingresar` link.
 *
 * The initials come from the profile endpoint because a layout never receives
 * the page data, the same reason the admin layout reads its own counters.
 */
export default async function AccountLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const profile = await getCustomerProfile();

  return (
    <div className="flex min-h-dvh flex-col bg-wf-canvas">
      <SiteHeader account={{ initials: profile.initials, label: "Mi cuenta" }} />
      <main className="flex-1">{children}</main>
      <SiteFooter />
    </div>
  );
}
