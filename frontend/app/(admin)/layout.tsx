import { AdminShell } from "@/features/admin";
import { getAdminNavigation } from "@/features/admin/services/admin.service";

/**
 * Admin shell layout: sidebar plus topbar for every screen under `/admin`.
 *
 * The counters come from their own endpoint because a layout never receives
 * the page props, and the shell is shared by the whole area.
 */
export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const counts = await getAdminNavigation();

  return <AdminShell counts={counts}>{children}</AdminShell>;
}
