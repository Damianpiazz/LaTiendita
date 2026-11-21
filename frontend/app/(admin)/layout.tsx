import { Suspense } from "react";
import { connection } from "next/server";

import { AdminShell } from "@/features/admin";
import { getAdminNavigation } from "@/features/admin/services/admin.service";

async function AdminShellContent({ children }: { children: React.ReactNode }) {
  // Protected admin data cannot be requested while the shell is prerendered.
  await connection();
  const counts = await getAdminNavigation();

  return <AdminShell counts={counts}>{children}</AdminShell>;
}

/**
 * Admin shell layout: sidebar plus topbar for every screen under `/admin`.
 * The counters come from their own endpoint because a layout never receives
 * the page props, and the shell is shared by the whole area.
 */
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <Suspense fallback={<div className="min-h-dvh bg-wf-canvas" aria-busy="true" />}>
      <AdminShellContent>{children}</AdminShellContent>
    </Suspense>
  );
}
