import type { CustomerProfile } from "@/features/customer/types";

/**
 * Identity card of `/mi-cuenta`: 64px avatar, name, contact line, status pill
 * with the member line, and the customer code on the right.
 *
 * The contact line keeps the frame's double spaces around the separator so the
 * rendered copy matches the design verbatim.
 */
export function ProfileCard({ profile }: { profile: CustomerProfile }) {
  return (
    <section className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-6 sm:flex-row sm:items-center sm:gap-6">
      <span
        aria-hidden
        className="flex size-16 shrink-0 items-center justify-center rounded-full bg-wf-subtle text-2xl font-bold text-wf-ink-secondary"
      >
        {profile.initials}
      </span>

      <div className="flex min-w-0 flex-1 flex-col gap-2">
        <h2 className="text-lg leading-[22px] font-bold text-wf-ink">{profile.name}</h2>
        <p className="text-sm leading-[17px] text-wf-ink-muted">
          {`${profile.email}  ·  ${profile.phone}`}
        </p>
        <div className="flex flex-wrap items-center gap-2">
          <span className="inline-flex h-6 items-center rounded-full bg-wf-inverse px-2 text-xs text-wf-on-inverse">
            {profile.statusLabel}
          </span>
          <span className="text-xs text-wf-ink-muted">{profile.memberSince}</span>
        </div>
      </div>

      <div className="flex shrink-0 flex-col gap-1 sm:items-end">
        <span className="text-xs text-wf-ink-muted">ID de cliente</span>
        <span className="text-sm font-medium text-wf-ink">{profile.customerId}</span>
      </div>
    </section>
  );
}
