import { AuthCard } from "@/features/auth/components/auth-card";
import { LoginForm } from "@/features/auth/components/login-form";

/** `Login Wireframe` of the design file. */
export function LoginPage() {
  return (
    <AuthCard
      title="Ingresá a tu cuenta"
      subtitle="Comprá más rápido y guardá tus favoritos."
    >
      <LoginForm />
    </AuthCard>
  );
}
