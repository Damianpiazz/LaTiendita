import type { CustomerProfile } from "@/features/customer/types";

/**
 * Page header of `/mi-cuenta`: greeting over the subtitle, with the outlined
 * `Editar perfil` control on the right.
 *
 * No profile edition flow exists in the mock, so the control is presentational
 * — the same criterion the admin page headers use for their actions.
 */
export function ProfileHeader({ profile }: { profile: CustomerProfile }) {
  return (
    <header className="flex flex-wrap items-center justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl leading-[29px] font-bold text-wf-ink">Hola, {profile.firstName}</h1>
        <p className="text-sm leading-[17px] text-wf-ink-muted">
          Revisá tus compras, tu actividad y tus datos en un solo lugar.
        </p>
      </div>

      <span className="flex h-10 shrink-0 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary">
        Editar perfil
      </span>
    </header>
  );
}
