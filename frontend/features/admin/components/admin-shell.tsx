import { AdminSidebar } from "@/features/admin/components/admin-sidebar";
import { AdminTopbar } from "@/features/admin/components/admin-topbar";
import type { AdminNavigationCounts } from "@/features/admin/types";

/**
 * Admin shell: sidebar plus topbar, without the public header, footer or
 * newsletter. Content keeps the frame's 32px padding.
 */
export function AdminShell({
  counts,
  children,
}: {
  counts: AdminNavigationCounts;
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-dvh bg-wf-canvas">
      <AdminSidebar counts={counts} />

      <div className="flex min-w-0 flex-1 flex-col">
        <AdminTopbar counts={counts} />
        <main className="flex-1 p-8">{children}</main>
      </div>
    </div>
  );
}
