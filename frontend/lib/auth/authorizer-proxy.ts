/** Shared helpers for the Next BFF routes that talk to Authorizer. */

import type { NextResponse } from "next/server";

/**
 * Authorizer rejects state-changing requests without Origin or Referer.
 * The browser Origin is the only one it accepts, so the BFF forwards it as-is.
 * MFA continuation also needs the HttpOnly cookies the first login response set.
 */
export function authorizerRequestHeaders(
  request: Request,
  includeCookies = false,
): Record<string, string> {
  const headers: Record<string, string> = {};
  const origin = request.headers.get("origin");

  if (origin) {
    headers.Origin = origin;
  }

  if (includeCookies) {
    const cookie = request.headers.get("cookie");

    if (cookie) {
      headers.Cookie = cookie;
    }
  }

  return headers;
}

/** Copies Authorizer's own Set-Cookie values to the browser response. */
export function forwardSetCookies(response: NextResponse, upstream: unknown): void {
  const values = Array.isArray(upstream)
    ? upstream
    : typeof upstream === "string"
      ? [upstream]
      : [];

  for (const value of values) {
    if (typeof value === "string" && value.trim()) {
      response.headers.append("set-cookie", value);
    }
  }
}

/**
 * Authorizer withholds the JWT when it offers first-time MFA enrollment.
 * The wireframes have no MFA screen, so callers continue via skip_mfa_setup.
 */
export function isMfaOffer(payload: unknown): boolean {
  if (!payload || typeof payload !== "object") {
    return false;
  }

  const record = payload as Record<string, unknown>;
  const token = typeof record.access_token === "string" ? record.access_token.trim() : "";

  if (token) {
    return false;
  }

  return (
    record.should_show_totp_screen === true ||
    record.should_show_email_otp_screen === true ||
    record.should_show_mobile_otp_screen === true ||
    String(record.message ?? "").toLowerCase().includes("mfa")
  );
}
