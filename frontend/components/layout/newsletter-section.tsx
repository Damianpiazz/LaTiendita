import { Button } from "@/components/ui/button";
import { siteConfig } from "@/config/site";

/**
 * Newsletter box.
 *
 * No subscription service exists yet, so the submit button is intentionally
 * inert: the wireframe renders the final layout without faking a success state.
 */
export function NewsletterSection() {
  const { newsletter } = siteConfig;

  return (
    <section aria-labelledby="newsletter-title" className="px-12 py-16">
      <div className="mx-auto flex max-w-[1440px] flex-col gap-6 rounded-wf-md bg-wf-inverse p-10 lg:flex-row lg:items-center lg:justify-between">
        <div className="max-w-lg">
          <h2 id="newsletter-title" className="text-2xl font-bold text-wf-on-inverse">
            {newsletter.title}
          </h2>
          <p className="mt-2 text-sm text-wf-muted">{newsletter.copy}</p>
        </div>

        <div className="flex w-full max-w-md flex-col gap-3 sm:flex-row">
          <label htmlFor="newsletter-email" className="sr-only">
            Email
          </label>
          <input
            id="newsletter-email"
            type="email"
            placeholder={newsletter.placeholder}
            className="h-12 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
          <Button
            type="button"
            className="h-12 shrink-0 bg-wf-surface px-6 text-sm font-semibold text-wf-ink hover:bg-wf-subtle"
          >
            {newsletter.action}
          </Button>
        </div>
      </div>
    </section>
  );
}
