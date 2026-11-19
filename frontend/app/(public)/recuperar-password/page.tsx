import type { Metadata } from "next";

import { PasswordRecoveryPage } from "@/features/auth";

export const metadata: Metadata = {
  title: "Recuperar contraseña",
};

export default function Page() {
  return <PasswordRecoveryPage />;
}
