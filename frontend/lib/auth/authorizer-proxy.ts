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

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  const parts = token.split(".");
  if (parts.length < 2) {
    return null;
  }

  try {
    const payload = Buffer.from(parts[1], "base64url").toString("utf8");
    const parsed = JSON.parse(payload);
    return parsed && typeof parsed === "object" ? (parsed as Record<string, unknown>) : null;
  } catch {
    return null;
  }
}

function readRoles(value: unknown): string[] {
  return Array.isArray(value)
    ? value.filter((role): role is string => typeof role === "string" && role.trim().length > 0)
    : [];
}

/**
 * Authorizer grants default instance roles on plain login. If the user can hold
 * higher roles (e.g. admin), re-login requesting all allowed roles so the JWT
 * carries the permissions the gateway enforces.
 */
export function rolesToElevate(payload: unknown): string[] | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const token = (payload as Record<string, unknown>).access_token;
  if (typeof token !== "string" || !token.trim()) {
    return null;
  }

  const claims = decodeJwtPayload(token);
  if (!claims) {
    return null;
  }

  const roles = readRoles(claims.roles);
  const allowedRoles = readRoles(claims.allowed_roles);
  if (allowedRoles.length === 0) {
    return null;
  }

  const granted = new Set(roles);
  const missing = allowedRoles.filter((role) => !granted.has(role));
  return missing.length > 0 ? allowedRoles : null;
}
