import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import {
  ACCESS_TOKEN_COOKIE,
  decodeUserCookie,
  USER_COOKIE,
} from "@/lib/auth/session";

/** Report only the non-sensitive user data; the JWT stays httpOnly. */
export async function GET() {
  const jar = await cookies();
  const hasToken = Boolean(jar.get(ACCESS_TOKEN_COOKIE)?.value);
  const user = hasToken ? decodeUserCookie(jar.get(USER_COOKIE)?.value) : null;

  return NextResponse.json({ authenticated: Boolean(user), user });
}
