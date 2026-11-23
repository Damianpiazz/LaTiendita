"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

export type ProfileActionState = {
  ok: boolean;
  message?: string;
  fieldErrors?: Record<string, string>;
};

function statusOf(error: unknown): number | undefined {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { status?: number } }).response;
    return typeof response?.status === "number" ? response.status : undefined;
  }

  return undefined;
}

function readText(value: FormDataEntryValue | null): string {
  return typeof value === "string" ? value.trim() : "";
}

async function authHeaders(next: string): Promise<{ Authorization: string }> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect(`/ingresar?next=${encodeURIComponent(next)}`);
  }

  return { Authorization: `Bearer ${token}` };
}

export async function updateProfile(
  _prevState: ProfileActionState,
  formData: FormData,
): Promise<ProfileActionState> {
  const headers = await authHeaders("/mi-cuenta/datos");
  const firstName = readText(formData.get("firstName"));
  const lastName = readText(formData.get("lastName"));
  const phone = readText(formData.get("phone"));
  const fieldErrors: Record<string, string> = {};

  if (!firstName) fieldErrors.firstName = "Ingresá tu nombre.";
  if (!lastName) fieldErrors.lastName = "Ingresá tu apellido.";
  if (phone.length > 30) fieldErrors.phone = "El teléfono no puede superar los 30 caracteres.";

  if (Object.keys(fieldErrors).length > 0) {
    return { ok: false, fieldErrors, message: "Revisá los campos marcados." };
  }

  try {
    await httpClient.patch(
      "/api/customers/me",
      { firstName, lastName, phone },
      { headers },
    );
  } catch (error) {
    if (statusOf(error) === 401 || statusOf(error) === 403) {
      redirect("/ingresar?next=%2Fmi-cuenta%2Fdatos");
    }

    return { ok: false, message: "No pudimos guardar tus datos. Intentá de nuevo." };
  }

  revalidatePath("/mi-cuenta");
  revalidatePath("/mi-cuenta/datos");

  return { ok: true, message: "Datos actualizados." };
}

export async function updateNewsletter(
  newsletterOptIn: boolean,
): Promise<ProfileActionState> {
  const headers = await authHeaders("/mi-cuenta/newsletter");

  try {
    await httpClient.put(
      "/api/customers/me/newsletter",
      { newsletterOptIn },
      { headers },
    );
  } catch (error) {
    if (statusOf(error) === 401 || statusOf(error) === 403) {
      redirect("/ingresar?next=%2Fmi-cuenta%2Fnewsletter");
    }

    return { ok: false, message: "No pudimos actualizar tu preferencia." };
  }

  revalidatePath("/mi-cuenta");
  revalidatePath("/mi-cuenta/newsletter");

  return { ok: true, message: newsletterOptIn ? "Newsletter activado." : "Newsletter desactivado." };
}
