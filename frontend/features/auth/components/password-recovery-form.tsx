import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFooter } from "@/features/auth/components/auth-footer";
import { AuthSubmit } from "@/features/auth/components/auth-submit";

/**
 * Recovery form of the `Password Recovery Wireframe` frame.
 *
 * The spam note is a card-level line (12/400 centred, 24px below the footer
 * block), not part of the footer, which is why it sits outside `AuthFooter`.
 * Nothing is sent: the mock has no auth endpoint.
 */
export function PasswordRecoveryForm() {
  return (
    <>
      <form className="flex flex-col gap-4">
        <AuthField
          id="recovery-email"
          name="email"
          type="email"
          label="Email"
          placeholder="tu@email.com"
          autoComplete="email"
        />
      </form>

      <AuthSubmit label="Enviar instrucciones" />

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
