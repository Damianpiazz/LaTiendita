import { AdminSidebar } from "@/features/admin/components/admin-sidebar";
import { AdminTopbar } from "@/features/admin/components/admin-topbar";
import type { AdminNavigationCounts } from "@/features/admin/types";
import type { SessionUser } from "@/lib/auth/session";

/**
 * Admin shell: sidebar plus topbar, without the public header, footer or
 * newsletter. Content keeps the frame's 32px padding.
 */
export function AdminShell({
  counts,
  user,
  children,
}: {
  counts: AdminNavigationCounts;
  user: SessionUser | null;
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-dvh bg-wf-canvas">
      <AdminSidebar counts={counts} user={user} />

      <div className="flex min-w-0 flex-1 flex-col">
        <AdminTopbar counts={counts} user={user} />
        <main className="flex-1 p-8">{children}</main>
      </div>
    </div>
  );
}
