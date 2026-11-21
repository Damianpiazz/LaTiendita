"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFooter } from "@/features/auth/components/auth-footer";
import { AuthSubmit } from "@/features/auth/components/auth-submit";
import { login } from "@/features/auth/services/auth.service";
import { SESSION_EVENT } from "@/lib/auth/session";

/**
 * Login form of the `Login Wireframe` frame, now wired to the real BFF.
 * Copy and spacing stay identical; errors are the only added state.
 */
export function LoginForm() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    const fields = new FormData(event.currentTarget);
    const email = String(fields.get("email") ?? "").trim();
    const password = String(fields.get("password") ?? "");

    try {
      await login(email, password);
      window.dispatchEvent(new Event(SESSION_EVENT));
      router.push("/carrito");
      router.refresh();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo iniciar sesión");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        <AuthField
          id="login-email"
          name="email"
          type="email"
          label="Email"
          placeholder="tu@email.com"
          autoComplete="email"
        />
        <AuthField
          id="login-password"
          name="password"
          type="password"
          label="Contraseña"
          placeholder="••••••••"
          autoComplete="current-password"
        />

        <div className="flex justify-end">
          <Link
            href="/recuperar-password"
            className="text-sm font-medium text-wf-ink-tertiary transition-colors hover:text-wf-ink"
          >
            ¿Olvidaste tu contraseña?
          </Link>
        </div>

        {error ? (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        ) : null}

        <AuthSubmit
          label={submitting ? "Ingresando..." : "Ingresar"}
          type="submit"
          disabled={submitting}
        />
      </form>

      <AuthFooter
        prompt="¿No tenés cuenta?"
        linkLabel="Creá tu cuenta"
        href="/crear-cuenta"
      />
    </>
  );
}
