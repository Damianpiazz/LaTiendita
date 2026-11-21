import { NextResponse } from "next/server";

import { ACCESS_TOKEN_COOKIE, USER_COOKIE } from "@/lib/auth/session";

/** Clears the session cookies; logout is local because Authorizer has no browser session here. */
export async function POST() {
  const response = NextResponse.json({ ok: true });

  response.cookies.set(ACCESS_TOKEN_COOKIE, "", { path: "/", maxAge: 0 });
  response.cookies.set(USER_COOKIE, "", { path: "/", maxAge: 0 });

  return response;
}
