import { AxiosError } from "axios";
import { NextResponse } from "next/server";

import {
  authorizerRequestHeaders,
  forwardSetCookies,
  isMfaOffer,
} from "@/lib/auth/authorizer-proxy";
import { parseAuthorizerAuth, setSessionCookies } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

function readStatus(error: unknown): number {
  if (error instanceof AxiosError && error.response?.status) {
    return error.response.status;
  }

  return 502;
}

/** Proxies Authorizer login and keeps the access token in an httpOnly cookie. */
export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as {
    email?: unknown;
    password?: unknown;
  };
  const email = typeof body.email === "string" ? body.email.trim() : "";
  const password = typeof body.password === "string" ? body.password : "";

  if (!email || !password) {
    return NextResponse.json(
      { message: "Email y contraseña son obligatorios" },
      { status: 400 },
    );
  }

  try {
    const { data, headers } = await httpClient.post<unknown>(
      "/api/auth/login",
      { email, password },
      { headers: authorizerRequestHeaders(request) },
    );

    if (isMfaOffer(data)) {
      const response = NextResponse.json({ mfaRequired: true, email });

      forwardSetCookies(
        response,
        (headers as Record<string, unknown> | undefined)?.["set-cookie"],
      );

      return response;
    }

    const session = parseAuthorizerAuth(data);
    const response = NextResponse.json({ user: session.user });

    setSessionCookies(response, session);

    return response;
  } catch (error) {
    const status = readStatus(error);

    return NextResponse.json(
      {
        message:
          status === 401
            ? "Email o contraseña incorrectos"
            : "No se pudo iniciar sesión",
      },
      { status },
    );
  }
}
