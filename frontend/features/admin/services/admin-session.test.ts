import { beforeEach, describe, expect, it, vi } from "vitest";

const { state } = vi.hoisted(() => ({
  state: {
    useMocks: false,
    token: "test-token" as string | null,
    user: null as string | null,
  },
}));

vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) => {
      if (name === "lt_access_token" && state.token) {
        return { value: state.token };
      }
      if (name === "lt_user" && state.user) {
        return { value: state.user };
      }
      return undefined;
    },
  }),
}));

vi.mock("@/config/env", () => ({
  env: {
    apiBaseUrl: "http://localhost:8081",
    get useMocks() {
      return state.useMocks;
    },
  },
}));

import { encodeUserCookie } from "@/lib/auth/session";
import { mockAuth } from "@/mocks/data/auth";

import { getAdminSessionUser } from "./admin.service";

const adminUser = {
  id: "elena-1",
  email: "elena.admin@latiendita.test",
  givenName: "Elena",
  familyName: "Administradora",
  roles: ["cliente", "admin"],
};

describe("getAdminSessionUser", () => {
  beforeEach(() => {
    state.useMocks = false;
    state.token = "test-token";
    state.user = encodeUserCookie(adminUser);
    mockAuth.logout();
  });

  it("reads the signed-in admin from the session cookies", async () => {
    await expect(getAdminSessionUser()).resolves.toEqual(adminUser);
  });

  it("ignores a stale user cookie without an access token", async () => {
    state.token = null;

    await expect(getAdminSessionUser()).resolves.toBeNull();
  });

  it("ignores a malformed user cookie", async () => {
    state.user = "not-a-cookie";

    await expect(getAdminSessionUser()).resolves.toBeNull();
  });

  it("uses the in-memory login in mock mode", async () => {
    state.useMocks = true;
    state.token = null;
    state.user = null;
    const mockUser = mockAuth.login();

    await expect(getAdminSessionUser()).resolves.toEqual(mockUser);
  });
});
