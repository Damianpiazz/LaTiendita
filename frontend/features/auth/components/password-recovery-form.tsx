"use client";

import { useState, type FormEvent } from "react";

import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFooter } from "@/features/auth/components/auth-footer";
import { AuthSubmit } from "@/features/auth/components/auth-submit";
import { requestPasswordReset } from "@/features/auth/services/auth.service";

/**
 * Recovery form of the `Password Recovery Wireframe` frame, wired to the real
 * Authorizer forgot-password flow. Copy, hint and spam note stay as drawn;
 * the confirmation message is the only added state.
 */
export function PasswordRecoveryForm() {
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage(null);
    setError(null);

    const fields = new FormData(event.currentTarget);
    const email = String(fields.get("email") ?? "").trim();
    setSubmitting(true);

    try {
      setMessage(await requestPasswordReset(email));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo enviar las instrucciones");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        <AuthField
          id="recovery-email"
          name="email"
          type="email"
          label="Email"
          placeholder="tu@email.com"
          autoComplete="email"
        />

        {message ? (
          <p role="status" className="text-sm text-wf-ink-secondary">
            {message}
          </p>
        ) : null}

        {error ? (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        ) : null}

        <AuthSubmit
          label={submitting ? "Enviando..." : "Enviar instrucciones"}
          type="submit"
          disabled={submitting}
        />
      </form>

      <AuthFooter
        prompt="¿Recordaste tu contraseña?"
        linkLabel="Ingresá"
        href="/ingresar"
      />

      <p className="text-center text-xs text-wf-ink-muted">
        Si no encontrás el email, revisá tu carpeta de spam.
      </p>
    </>
  );
}
