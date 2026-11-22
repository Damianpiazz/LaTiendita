import { AxiosError } from "axios";

import type { SessionUser } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

type AuthResponse = {
  user?: SessionUser;
  mfaRequired?: boolean;
  email?: string;
};

type SessionResponse = {
  authenticated: boolean;
  user: SessionUser | null;
};

export type SignupInput = {
  name: string;
  email: string;
  password: string;
};

function readErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as { message?: string } | undefined;

    if (data?.message) {
      return data.message;
    }
  }

  return error instanceof Error && error.message ? error.message : fallback;
}

/** Sends credentials to the same-origin BFF; the JWT never reaches the browser. */
export async function login(email: string, password: string): Promise<SessionUser> {
  try {
    const { data } = await httpClient.post<AuthResponse>(
      "/api/auth/login",
      { email, password },
      { baseURL: "" },
    );

    if (data.mfaRequired) {
      const { data: skipped } = await httpClient.post<AuthResponse>(
        "/api/auth/skip-mfa",
        { email: data.email ?? email, password },
        { baseURL: "" },
      );

      if (!skipped.user) {
        throw new Error("No se pudo completar el ingreso");
      }

      return skipped.user;
    }

    if (!data.user) {
      throw new Error("No se pudo iniciar sesión");
    }

    return data.user;
  } catch (error) {
    throw new Error(readErrorMessage(error, "No se pudo iniciar sesión"));
  }
}

/** Registers a customer through the BFF; the name is split into Authorizer fields there. */
export async function signup({ name, email, password }: SignupInput): Promise<SessionUser> {
  try {
    const { data } = await httpClient.post<AuthResponse>(
      "/api/auth/signup",
      { name, email, password },
      { baseURL: "" },
    );

    if (data.mfaRequired) {
      const { data: skipped } = await httpClient.post<AuthResponse>(
        "/api/auth/skip-mfa",
        { email: data.email ?? email, password },
        { baseURL: "" },
      );

      if (!skipped.user) {
        throw new Error("No se pudo completar el ingreso");
      }

      return skipped.user;
    }

    if (!data.user) {
      throw new Error("No se pudo crear la cuenta");
    }

    return data.user;
  } catch (error) {
    throw new Error(readErrorMessage(error, "No se pudo crear la cuenta"));
  }
}

/** Reads the non-sensitive session snapshot used by the public header. */
export async function getSession(): Promise<SessionResponse> {
  try {
    const { data } = await httpClient.get<SessionResponse>("/api/auth/session", {
      baseURL: "",
    });

    return data;
  } catch {
    return { authenticated: false, user: null };
  }
}

/** Sends the recovery email to Authorizer through the BFF. */
export async function requestPasswordReset(email: string): Promise<string> {
  try {
    const { data } = await httpClient.post<{ message?: string }>(
      "/api/auth/forgot-password",
      { email },
      { baseURL: "" },
    );

    if (!data.message) {
      throw new Error("No se pudo enviar las instrucciones");
    }

    return data.message;
  } catch (error) {
    throw new Error(readErrorMessage(error, "No se pudo enviar las instrucciones"));
  }
}

export async function logout(): Promise<void> {
  await httpClient.post("/api/auth/logout", undefined, { baseURL: "" });
}
