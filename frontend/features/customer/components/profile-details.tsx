import Link from "next/link";

import { NewsletterSwitch } from "@/features/customer/components/newsletter-switch";
import type { CustomerAddress, CustomerPreferences } from "@/features/customer/types";

/**
 * Bottom row of `/mi-cuenta`: the default address and the notification
 * preferences, two equal cards 24px apart with 20px padding and 16px gaps.
 */
export function ProfileDetails({
  address,
  preferences,
}: {
  address: CustomerAddress;
  preferences: CustomerPreferences;
}) {
  return (
    <section className="grid gap-6 md:grid-cols-2">
      <article className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5">
        <header className="flex items-center justify-between gap-4">
          <h2 className="text-base leading-[19px] font-bold text-wf-ink">Dirección principal</h2>
          <Link
            href="/mi-cuenta/direcciones"
            className="text-sm leading-[17px] text-wf-ink-secondary underline-offset-2 hover:underline"
          >
            Editar
          </Link>
        </header>

        {address.lines.length === 0 ? (
          <p className="text-sm leading-[17px] text-wf-ink-muted">
            Sin dirección registrada.
          </p>
        ) : (
          <div className="flex flex-col gap-1">
            <span className="text-sm leading-[17px] text-wf-ink-secondary">
              {address.recipient}
            </span>
            {address.lines.map((line) => (
              <span key={line} className="text-sm leading-[17px] text-wf-ink-muted">
                {line}
              </span>
            ))}
          </div>
        )}

        {address.isDefault ? (
          <span className="inline-flex h-6 w-fit items-center rounded-full bg-wf-subtle px-2 text-xs text-wf-ink-secondary">
            Predeterminada
          </span>
        ) : null}
      </article>

      <article className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5">
        <header className="flex items-center justify-between gap-4">
          <h2 className="text-base leading-[19px] font-bold text-wf-ink">Preferencias</h2>
          <Link
            href="/mi-cuenta/newsletter"
            className="text-sm leading-[17px] text-wf-ink-secondary underline-offset-2 hover:underline"
          >
            Editar
          </Link>
        </header>

        <div className="flex items-center justify-between gap-4">
          <span className="text-sm leading-[17px] text-wf-ink-secondary">Newsletter</span>
          <NewsletterSwitch initialValue={preferences.newsletter} compact />
        </div>

        <p className="text-sm leading-[17px] text-wf-ink-muted">
          Recibí novedades y ofertas por email.
        </p>

        <div className="flex items-center gap-3">
          <span aria-hidden className="block size-6 shrink-0 rounded-wf-sm bg-wf-inverse" />
          <span className="text-xs text-wf-ink-muted">{preferences.consentNote}</span>
        </div>
      </article>
    </section>
  );
}
