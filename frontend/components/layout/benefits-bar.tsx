import { siteConfig } from "@/config/site";

/**
 * Trust strip measured from the `Benefits` frame (0:476): plain white band,
 * 40px vertical padding, 48px gutter, 24px gap, 44px icon placeholders.
 */
export function BenefitsBar() {
  return (
    <section aria-label="Beneficios de comprar en La Tiendita" className="bg-wf-surface px-12 py-10">
      <ul className="mx-auto grid w-full max-w-[1440px] gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {siteConfig.benefits.map((benefit) => (
          <li key={benefit.title} className="flex items-center gap-3.5">
            <span
              aria-hidden
              className="size-11 shrink-0 rounded-wf-sm bg-wf-subtle"
            />
            <span className="flex flex-col gap-0.5">
              <span className="text-sm font-bold text-wf-ink">{benefit.title}</span>
              <span className="text-xs text-wf-ink-muted">{benefit.note}</span>
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
