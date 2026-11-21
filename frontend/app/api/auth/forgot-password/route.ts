import { AxiosError } from "axios";
import { NextResponse } from "next/server";

import { authorizerRequestHeaders } from "@/lib/auth/authorizer-proxy";
import { httpClient } from "@/lib/http/client";

function readStatus(error: unknown): number {
  if (error instanceof AxiosError && error.response?.status) {
    return error.response.status;
  }

  return 502;
}

/** Proxies Authorizer's forgot-password endpoint and keeps its response copy. */
export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as { email?: unknown };
  const email = typeof body.email === "string" ? body.email.trim() : "";

  if (!email) {
    return NextResponse.json({ message: "Email es obligatorio" }, { status: 400 });
  }

  try {
    const { data } = await httpClient.post<{ message?: string }>(
      "/api/auth/forgot_password",
      { email },
      { headers: authorizerRequestHeaders(request) },
    );

    return NextResponse.json({
      message: data.message ?? "Revisá tu correo para continuar.",
    });
  } catch (error) {
    const status = readStatus(error);

    return NextResponse.json(
      { message: "No se pudo enviar las instrucciones" },
      { status },
    );
  }
}
