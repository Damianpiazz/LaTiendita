"use client";

import { useActionState } from "react";

import {
  updateProfile,
  type ProfileActionState,
} from "@/features/customer/actions/profile.actions";
import type { CustomerProfile } from "@/features/customer/types";

const INITIAL_STATE: ProfileActionState = { ok: false };

/** Personal data form, saved through the authenticated customer BFF action. */
export function ProfileForm({ profile }: { profile: CustomerProfile }) {
  const [state, formAction, isPending] = useActionState(updateProfile, INITIAL_STATE);

  return (
    <form
      action={formAction}
      noValidate
      className="flex max-w-2xl flex-col gap-5 rounded-wf-md border border-wf-line bg-wf-surface p-6"
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <TextField
          id="profile-first-name"
          name="firstName"
          label="Nombre"
          defaultValue={profile.firstName}
          error={state.fieldErrors?.firstName}
          required
        />
        <TextField
          id="profile-last-name"
          name="lastName"
          label="Apellido"
          defaultValue={profile.lastName ?? profile.name.replace(profile.firstName, "").trim()}
          error={state.fieldErrors?.lastName}
          required
        />
      </div>

      <TextField
        id="profile-phone"
        name="phone"
        label="Teléfono"
        type="tel"
        defaultValue={profile.phone === "Sin registrar" ? "" : profile.phone}
        error={state.fieldErrors?.phone}
      />

      <p className="text-sm text-wf-ink-muted">Email: {profile.email}</p>

      {state.message ? (
        <p role={state.ok ? "status" : "alert"} className={state.ok ? "text-sm text-green-700" : "text-sm text-red-600"}>
          {state.message}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={isPending}
        className="h-10 self-start rounded-wf-sm bg-wf-inverse px-5 text-sm font-medium text-wf-on-inverse disabled:opacity-60"
      >
        {isPending ? "Guardando..." : "Guardar cambios"}
      </button>
    </form>
  );
}

function TextField({
  id,
  name,
  label,
  defaultValue,
  error,
  type = "text",
  required,
}: {
  id: string;
  name: string;
  label: string;
  defaultValue: string;
  error?: string;
  type?: string;
  required?: boolean;
}) {
  return (
    <div className="flex flex-col gap-2">
      <label htmlFor={id} className="text-sm font-medium text-wf-ink-secondary">
        {label}
      </label>
      <input
        id={id}
        name={name}
        type={type}
        defaultValue={defaultValue}
        required={required}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? `${id}-error` : undefined}
        className="h-10 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink focus-visible:border-wf-inverse focus-visible:outline-none"
      />
      {error ? (
        <p id={`${id}-error`} className="text-xs text-red-600">
          {error}
        </p>
      ) : null}
    </div>
  );
}
