import { AuthCard } from "@/features/auth/components/auth-card";
import { PasswordRecoveryForm } from "@/features/auth/components/password-recovery-form";

/** `Password Recovery Wireframe` of the design file. */
export function PasswordRecoveryPage() {
  return (
    <AuthCard
      title="Recuperá tu contraseña"
      subtitle="Ingresá tu email y te enviamos las instrucciones."
    >
      <PasswordRecoveryForm />
    </AuthCard>
  );
}
