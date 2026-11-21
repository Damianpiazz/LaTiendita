import { SiteFooter } from "@/components/layout/site-footer";
import { SiteHeaderSession } from "@/components/layout/site-header-session";

/**
 * Customer account shell: the public header and footer, with the header chip
 * following the real session on the client. Reading the session here instead
 * of in the layout keeps the route prerenderable under Cache Components.
 */
export default function AccountLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-dvh flex-col bg-wf-canvas">
      <SiteHeaderSession />
      <main className="flex-1">{children}</main>
      <SiteFooter />
    </div>
  );
}
