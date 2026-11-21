"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFooter } from "@/features/auth/components/auth-footer";
import { AuthSubmit } from "@/features/auth/components/auth-submit";
import { signup } from "@/features/auth/services/auth.service";
import { SESSION_EVENT } from "@/lib/auth/session";

/**
 * Registration form of the `Register Wireframe` frame, wired to real signup.
 * Copy, hint and 16px checkbox stay as drawn; errors are the only added state.
 */
export function RegisterForm() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const fields = new FormData(event.currentTarget);
    const name = String(fields.get("name") ?? "").trim();
    const email = String(fields.get("email") ?? "").trim();
    const password = String(fields.get("password") ?? "");
    const confirmation = String(fields.get("passwordConfirmation") ?? "");

    if (!fields.get("terms")) {
      setError("Debés aceptar los términos y condiciones");
      return;
    }

    if (password !== confirmation) {
      setError("Las contraseñas no coinciden");
      return;
    }

    setSubmitting(true);

    try {
      await signup({ name, email, password });
      window.dispatchEvent(new Event(SESSION_EVENT));
      router.push("/carrito");
      router.refresh();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo crear la cuenta");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        <AuthField
          id="register-name"
          name="name"
          label="Nombre y apellido"
          placeholder="Ana Pérez"
          autoComplete="name"
        />
        <AuthField
          id="register-email"
          name="email"
          type="email"
          label="Email"
          placeholder="tu@email.com"
          autoComplete="email"
        />
        <AuthField
          id="register-password"
          name="password"
          type="password"
          label="Contraseña"
          placeholder="••••••••"
          autoComplete="new-password"
          hint="Mínimo 8 caracteres, una mayúscula y un número."
        />
        <AuthField
          id="register-password-confirmation"
          name="passwordConfirmation"
          type="password"
          label="Confirmar contraseña"
          placeholder="••••••••"
          autoComplete="new-password"
        />

        <label className="flex cursor-pointer items-center gap-3">
          <input
            type="checkbox"
            name="terms"
            className="size-4 shrink-0 cursor-pointer appearance-none border border-wf-line-strong bg-wf-surface checked:border-wf-inverse checked:bg-wf-inverse focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-wf-inverse focus-visible:ring-offset-2"
          />
          <span className="text-sm text-wf-ink-muted">
            Acepto los términos y condiciones y la política de privacidad
          </span>
        </label>

        {error ? (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        ) : null}

        <AuthSubmit
          label={submitting ? "Creando cuenta..." : "Crear cuenta"}
          type="submit"
          disabled={submitting}
        />
      </form>

      <AuthFooter
        prompt="¿Ya tenés cuenta?"
        linkLabel="Ingresá"
        href="/ingresar"
      />
    </>
  );
}
