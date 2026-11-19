import { AuthCard } from "@/features/auth/components/auth-card";
import { RegisterForm } from "@/features/auth/components/register-form";

/** `Register Wireframe` of the design file. */
export function RegisterPage() {
  return (
    <AuthCard
      title="Creá tu cuenta"
      subtitle="Completá tus datos para comprar en La Tiendita."
    >
      <RegisterForm />
    </AuthCard>
  );
}
