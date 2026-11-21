import type { SessionUser } from "../../lib/auth/session";

/** In-memory session so mock mode still exercises login, header and logout. */
let currentUser: SessionUser | null = null;

const mockUser: SessionUser = {
  id: "mock-user-1",
  email: "cliente@example.com",
  givenName: "Ana",
  familyName: "Pérez",
  roles: ["cliente"],
};

export const mockAuth = {
  login(): SessionUser {
    currentUser = { ...mockUser };

    return { ...currentUser };
  },
  session(): { authenticated: boolean; user: SessionUser | null } {
    return {
      authenticated: currentUser !== null,
      user: currentUser ? { ...currentUser } : null,
    };
  },
  logout(): { ok: true } {
    currentUser = null;

    return { ok: true };
  },
};
