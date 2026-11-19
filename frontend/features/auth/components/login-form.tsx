import Link from "next/link";

import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFooter } from "@/features/auth/components/auth-footer";
import { AuthSubmit } from "@/features/auth/components/auth-submit";

/**
 * Login form of the `Login Wireframe` frame.
 *
 * The frame defines no submit target and no error state, so the fields are
 * native and presentational; `¿Olvidaste tu contraseña?` is a real link because
 * the recovery screen exists.
 */
export function LoginForm() {
  return (
    <>
      <form className="flex flex-col gap-4">
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
      </form>

      <AuthSubmit label="Ingresar" />

      <AuthFooter
        prompt="¿No tenés cuenta?"
        linkLabel="Creá tu cuenta"
        href="/crear-cuenta"
      />
    </>
  );
}
