"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

export type ReviewActionState = {
  ok: boolean;
  message?: string;
  fieldErrors?: Record<string, string>;
};

async function authHeaders(next: string): Promise<{ Authorization: string }> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect(`/ingresar?next=${encodeURIComponent(next)}`);
  }

  return { Authorization: `Bearer ${token}` };
}

function statusOf(error: unknown): number | undefined {
  if (error && typeof error === "object" && "response" in error) {
    return (error as { response?: { status?: number } }).response?.status;
  }

  return undefined;
}

export async function createReview(
  _prevState: ReviewActionState,
  formData: FormData,
): Promise<ReviewActionState> {
  const productId = String(formData.get("productId") ?? "").trim();
  const slug = String(formData.get("slug") ?? "").trim();
  const next = slug ? `/productos/${slug}` : "/";
  const headers = await authHeaders(next);
  const rating = Number(formData.get("rating"));
  const title = String(formData.get("title") ?? "").trim();
  const body = String(formData.get("body") ?? "").trim();
  const fieldErrors: Record<string, string> = {};

  if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
    fieldErrors.rating = "Elegí una puntuación de 1 a 5.";
  }
  if (!title) fieldErrors.title = "Ingresá un título.";
  if (!body) fieldErrors.body = "Contanos tu experiencia.";

  if (Object.keys(fieldErrors).length > 0) {
    return { ok: false, fieldErrors, message: "Revisá los campos marcados." };
  }

  try {
    await httpClient.post(
      "/api/reviews",
      { productId: Number(productId), rating, title, body },
      { headers },
    );
  } catch (error) {
    const status = statusOf(error);

    if (status === 401 || status === 403) {
      redirect(`/ingresar?next=${encodeURIComponent(next)}`);
    }

    if (status === 409) {
      return { ok: false, message: "Ya publicaste una reseña para este producto." };
    }

    return {
      ok: false,
      message: "No pudimos enviar la reseña. Verificá que hayas comprado este producto.",
    };
  }

  if (slug) revalidatePath(`/productos/${slug}`);
  revalidatePath("/mi-cuenta");

  return { ok: true, message: "Reseña enviada. Se publicará después de la moderación." };
}

export async function markReviewHelpful(
  reviewId: string,
): Promise<{ ok: boolean; message?: string }> {
  const headers = await authHeaders("/");

  try {
    await httpClient.post(`/api/reviews/${encodeURIComponent(reviewId)}/helpful`, {}, { headers });
  } catch (error) {
    const status = statusOf(error);

    if (status === 401 || status === 403) {
      redirect("/ingresar");
    }

    return { ok: false, message: "No pudimos registrar tu voto." };
  }

  return { ok: true };
}
