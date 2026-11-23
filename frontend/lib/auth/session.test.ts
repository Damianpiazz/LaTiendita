import { describe, expect, it } from "vitest";

import {
  ACCESS_TOKEN_COOKIE,
  USER_COOKIE,
  decodeUserCookie,
  encodeUserCookie,
  parseAuthorizerAuth,
  sessionCookieOptions,
  sessionFullName,
  sessionInitials,
  sessionRoleLabel,
} from "./session";

const authorizerPayload = {
  access_token: "jwt-de-prueba",
  id_token: "id-token",
  refresh_token: "refresh-token",
  expires_in: 3600,
  user: {
    id: "user-1",
    email: "cliente@example.com",
    given_name: "Ana",
    family_name: "Pérez",
    roles: ["cliente"],
  },
};

describe("Authorizer session mapping", () => {
  it("maps the real v1AuthResponse into the session shape", () => {
    expect(parseAuthorizerAuth(authorizerPayload)).toEqual({
      accessToken: "jwt-de-prueba",
      expiresIn: 3600,
      user: {
        id: "user-1",
        email: "cliente@example.com",
        givenName: "Ana",
        familyName: "Pérez",
        roles: ["cliente"],
      },
    });
  });

  it("rejects an empty token or a missing user", () => {
    expect(() => parseAuthorizerAuth({ ...authorizerPayload, access_token: "" })).toThrow();
    expect(() => parseAuthorizerAuth({ ...authorizerPayload, user: undefined })).toThrow();
  });

  it("stores the JWT in an httpOnly cookie and the UI user in a readable one", () => {
    const options = sessionCookieOptions(7200);

    expect(ACCESS_TOKEN_COOKIE).toBe("lt_access_token");
    expect(USER_COOKIE).toBe("lt_user");
    expect(options.httpOnly).toBe(true);
    expect(options.sameSite).toBe("lax");
    expect(options.maxAge).toBe(7200);

    const user = parseAuthorizerAuth(authorizerPayload).user;
    expect(decodeUserCookie(encodeUserCookie(user))).toEqual(user);
    expect(decodeUserCookie(undefined)).toBeNull();
  });
});

describe("session display helpers", () => {
  const user = {
    id: "user-1",
    email: "elena.admin@latiendita.test",
    givenName: "Elena",
    familyName: "Administradora",
    roles: ["cliente", "admin"],
  };

  it("derives initials and a full name from the session", () => {
    expect(sessionInitials(user)).toBe("EA");
    expect(sessionFullName(user)).toBe("Elena Administradora");
  });

  it("falls back to the email when the name is missing", () => {
    const anonymous = { ...user, givenName: undefined, familyName: undefined };

    expect(sessionInitials(anonymous)).toBe("EL");
    expect(sessionFullName(anonymous)).toBe("elena.admin");
  });

  it("labels the admin role even when the client role is also present", () => {
    expect(sessionRoleLabel(user)).toBe("Administrador");
    expect(sessionRoleLabel({ ...user, roles: ["cliente"] })).toBe("Cliente");
    expect(sessionRoleLabel({ ...user, roles: [] })).toBe("Usuario");
  });
});
