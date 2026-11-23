import { BenefitsBar } from "@/components/layout/benefits-bar";
import { AccountBreadcrumb } from "@/features/customer/components/account-breadcrumb";
import { AccountNav } from "@/features/customer/components/account-nav";

/**
 * Shared shell for the `/mi-cuenta` subroutes: same breadcrumb, 240px nav card
 * and benefits band as the profile screen, with a single content column.
 */
export function AccountPageShell({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <AccountBreadcrumb />

      <section className="bg-wf-canvas">
        <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-6 px-6 pb-16 pt-10 lg:flex-row lg:px-12">
          <AccountNav />

          <div className="flex min-w-0 flex-1 flex-col gap-8">
            <header className="flex flex-col gap-2">
              <h1 className="text-2xl font-bold text-wf-ink">{title}</h1>
              <p className="text-sm text-wf-ink-muted">{description}</p>
            </header>
            {children}
          </div>
        </div>
      </section>

      <BenefitsBar />
    </div>
  );
}
