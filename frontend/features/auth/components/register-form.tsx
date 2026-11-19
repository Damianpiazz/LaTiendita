import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFooter } from "@/features/auth/components/auth-footer";
import { AuthSubmit } from "@/features/auth/components/auth-submit";

/**
 * Registration form of the `Register Wireframe` frame.
 *
 * Fields are native and presentational, like the rest of the mock: the design
 * defines no submit target and no error state. The password rules sit under the
 * field as a hint instead of being validated, and the terms checkbox is drawn
 * as a 16px square with a 12px gap to its copy.
 */
export function RegisterForm() {
  return (
    <>
      <form className="flex flex-col gap-4">
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
      </form>

      <AuthSubmit label="Crear cuenta" />

      <AuthFooter
        prompt="¿Ya tenés cuenta?"
        linkLabel="Ingresá"
        href="/ingresar"
      />
    </>
  );
}
