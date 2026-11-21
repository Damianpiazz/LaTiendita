import { SiteFooter } from "@/components/layout/site-footer";
import { SiteHeaderSession } from "@/components/layout/site-header-session";

/** Public shell: header, page content and footer. */
export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-dvh flex-col bg-wf-canvas">
      <SiteHeaderSession />
      <main className="flex-1">{children}</main>
      <SiteFooter />
    </div>
  );
}
