import { AxiosError } from "axios";
import { NextResponse } from "next/server";

import { authorizerRequestHeaders, rolesToElevate } from "@/lib/auth/authorizer-proxy";
import { parseAuthorizerAuth, setSessionCookies } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

function readStatus(error: unknown): number {
  if (error instanceof AxiosError && error.response?.status) {
    return error.response.status;
  }

  return 502;
}

/**
 * Completes the first-time MFA offer without a new wireframe screen.
 * Authorizer identifies the pending login through the mfa_session cookies
 * the browser received from the login response.
 */
export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as {
    email?: unknown;
    password?: unknown;
  };
  const email = typeof body.email === "string" ? body.email.trim() : "";
  const password = typeof body.password === "string" ? body.password : "";

  if (!email) {
    return NextResponse.json({ message: "Email es obligatorio" }, { status: 400 });
  }

  try {
    const requestHeaders = authorizerRequestHeaders(request, true);
    const { data } = await httpClient.post<unknown>(
      "/api/auth/skip_mfa_setup",
      { email },
      { headers: requestHeaders },
    );

    const elevatedRoles = password ? rolesToElevate(data) : null;
    const authPayload =
      elevatedRoles && password
        ? (
            await httpClient.post<unknown>(
              "/api/auth/login",
              { email, password, roles: elevatedRoles },
              { headers: authorizerRequestHeaders(request) },
            )
          ).data
        : data;

    const session = parseAuthorizerAuth(authPayload);
    const response = NextResponse.json({ user: session.user });

    setSessionCookies(response, session);

    return response;
  } catch (error) {
    const status = readStatus(error);

    return NextResponse.json(
      { message: "No se pudo completar el ingreso" },
      { status },
    );
  }
}
