/** Session contract shared by the Next BFF route handlers and the browser UI. */

import type { NextResponse } from "next/server";

export const ACCESS_TOKEN_COOKIE = "lt_access_token";
export const USER_COOKIE = "lt_user";
export const SESSION_EVENT = "lt-session-change";

export type SessionUser = {
  id: string;
  email: string;
  givenName?: string;
  familyName?: string;
  roles: string[];
};

export type Session = {
  accessToken: string;
  expiresIn: number;
  user: SessionUser;
};

export type SessionCookieOptions = {
  httpOnly: boolean;
  sameSite: "lax";
  secure: boolean;
  path: string;
  maxAge: number;
};

function readRecord(value: unknown): Record<string, unknown> | null {
  return value && typeof value === "object" ? (value as Record<string, unknown>) : null;
}

function readText(value: unknown): string | undefined {
  return typeof value === "string" && value.trim() ? value.trim() : undefined;
}

function readUser(value: unknown): SessionUser | null {
  const record = readRecord(value);
  const id = readText(record?.id);
  const email = readText(record?.email);

  if (!id || !email) {
    return null;
  }

  const roles = Array.isArray(record?.roles)
    ? record.roles.filter((role): role is string => typeof role === "string")
    : [];

  return {
    id,
    email,
    givenName: readText(record?.given_name ?? record?.givenName),
    familyName: readText(record?.family_name ?? record?.familyName),
    roles,
  };
}

/** Maps the live Authorizer `v1AuthResponse` to the shape used by the app. */
export function parseAuthorizerAuth(payload: unknown): Session {
  const record = readRecord(payload);
  const accessToken = readText(record?.access_token ?? record?.accessToken);

  if (!accessToken) {
    throw new Error("Authorizer response has no access token");
  }

  const user = readUser(record?.user);

  if (!user) {
    throw new Error("Authorizer response has no user");
  }

  const expiresIn = Number(record?.expires_in ?? record?.expiresIn ?? 3600);

  return {
    accessToken,
    expiresIn: Number.isFinite(expiresIn) && expiresIn > 0 ? expiresIn : 3600,
    user,
  };
}

export function sessionCookieOptions(expiresIn: number): SessionCookieOptions {
  return {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: expiresIn,
  };
}

export function userCookieOptions(expiresIn: number): SessionCookieOptions {
  return { ...sessionCookieOptions(expiresIn), httpOnly: false };
}

/** Writes both session cookies on a BFF response, in one place for every flow. */
export function setSessionCookies(response: NextResponse, session: Session): void {
  response.cookies.set(
    ACCESS_TOKEN_COOKIE,
    session.accessToken,
    sessionCookieOptions(session.expiresIn),
  );
  response.cookies.set(
    USER_COOKIE,
    encodeUserCookie(session.user),
    userCookieOptions(session.expiresIn),
  );
}

export function encodeUserCookie(user: SessionUser): string {
  return encodeURIComponent(JSON.stringify(user));
}

export function decodeUserCookie(raw: string | undefined): SessionUser | null {
  if (!raw) {
    return null;
  }

  try {
    return readUser(JSON.parse(decodeURIComponent(raw)));
  } catch {
    return null;
  }
}

export function sessionInitials(user: SessionUser): string {
  const initials = `${user.givenName?.[0] ?? ""}${user.familyName?.[0] ?? ""}`.toUpperCase();

  return initials || user.email.slice(0, 2).toUpperCase();
}

export function sessionLabel(user: SessionUser): string {
  return user.givenName ?? user.email.split("@")[0];
}

/** Longer identity for places where a first name alone would be ambiguous. */
export function sessionFullName(user: SessionUser): string {
  const fullName = `${user.givenName ?? ""} ${user.familyName ?? ""}`.trim();

  return fullName || user.email.split("@")[0];
}

/** Human-facing role for the admin shell; admin takes precedence over client. */
export function sessionRoleLabel(user: SessionUser): string {
  const roles = user.roles.map((role) => role.trim().toLowerCase());

  if (roles.includes("admin")) {
    return "Administrador";
  }

  if (roles.includes("cliente")) {
    return "Cliente";
  }

  return "Usuario";
}
