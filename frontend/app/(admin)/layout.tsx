import { Suspense } from "react";
import { connection } from "next/server";

import { AdminShell } from "@/features/admin";
import {
  getAdminNavigation,
  getAdminSessionUser,
} from "@/features/admin/services/admin.service";

async function AdminShellContent({ children }: { children: React.ReactNode }) {
  // Protected admin data cannot be requested while the shell is prerendered.
  await connection();
  const [counts, user] = await Promise.all([
    getAdminNavigation(),
    getAdminSessionUser(),
  ]);

  return <AdminShell counts={counts} user={user}>{children}</AdminShell>;
}

/**
 * Admin shell layout: sidebar plus topbar for every screen under `/admin`.
 * The counters and the signed-in identity come from their own sources because
 * a layout never receives the page props, and the shell is shared by the area.
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
