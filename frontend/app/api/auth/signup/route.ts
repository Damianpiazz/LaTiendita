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

/** Maps the wireframe signup form to Authorizer and keeps the JWT httpOnly. */
export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as {
    name?: unknown;
    email?: unknown;
    password?: unknown;
  };
  const name = typeof body.name === "string" ? body.name.trim() : "";
  const email = typeof body.email === "string" ? body.email.trim() : "";
  const password = typeof body.password === "string" ? body.password : "";

  if (!name || !email || !password) {
    return NextResponse.json(
      { message: "Nombre, email y contraseña son obligatorios" },
      { status: 400 },
    );
  }

  const parts = name.split(/\s+/);
  const givenName = parts[0] ?? "";
  const familyName = parts.slice(1).join(" ");

  try {
    const { data, headers } = await httpClient.post<unknown>(
      "/api/auth/signup",
      {
        email,
        password,
        confirm_password: password,
        given_name: givenName,
        family_name: familyName,
        roles: ["cliente"],
      },
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
    const data = error instanceof AxiosError
      ? (error.response?.data as { message?: string } | undefined)
      : undefined;

    return NextResponse.json(
      {
        message:
          data?.message ??
          (status === 400
            ? "No se pudo crear la cuenta"
            : "No se pudo completar el registro"),
      },
      { status },
    );
  }
}
